package de.deroq.clans;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.model.Clan;
import de.deroq.clans.repository.ClanDataRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 09.12.2022
 */
@RequiredArgsConstructor
public class ClanManager {

    private final ClanDataRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<AbstractClan>> clanByIdCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, ListenableFuture<AbstractClan>>() {
                @Override
                public ListenableFuture<AbstractClan> load(UUID id) {
                    return repository.getClanById(id);
                }
            });

    @Getter
    private final LoadingCache<String, ListenableFuture<UUID>> clanByNameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(String clanName) {
                    return repository.getClanByName(clanName);
                }
            });

    @Getter
    private final LoadingCache<String, ListenableFuture<UUID>> clanByTagCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(String clanTag) {
                    return repository.getClanByTag(clanTag);
                }
            });

    @Getter
    private final LoadingCache<UUID, ListenableFuture<UUID>> clanByPlayerCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(UUID player) {
                    return repository.getClanByPlayer(player);
                }
            });

    public ListenableFuture<AbstractClan> createClan(ClanSystem clanSystem, AbstractUser user, String clanName, String clanTag) {
        UUID id = UUID.randomUUID();
        Clan clan = new Clan(
                clanSystem,
                id,
                clanName,
                clanTag,
                Collections.singletonMap(user.getUuid(), Clan.Group.LEADER)
        );
        clanByIdCache.put(id, Futures.immediateFuture(clan));
        clanByNameCache.put(clanName.toLowerCase(), Futures.immediateFuture(id));
        clanByTagCache.put(clanTag.toLowerCase(), Futures.immediateFuture(id));
        clanByPlayerCache.put(user.getUuid(), Futures.immediateFuture(id));
        clanSystem.getUserManager().setClan(user, id);
        return repository.createClan(user.getUuid(), clan);
    }

    public ListenableFuture<Boolean> deleteClan(ClanSystem clanSystem, AbstractClan clan) {
        clanByIdCache.invalidate(clan.getClanId());
        clanByNameCache.invalidate(clan.getClanName());
        clanByTagCache.invalidate(clan.getClanTag());
        clan.getOnlinePlayers().forEach(uuid -> {
            ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
            Callback.of(userFuture, onlineUser -> clanSystem.getUserManager().setClan(onlineUser, null));
        });
        clanSystem.getInviteManager().removeInvitesByClan(clan.getClanId());
        return repository.deleteClan(clan);
    }

    public ListenableFuture<Boolean> leaveClan(ClanSystem clanSystem, AbstractUser user, AbstractClan clan) {
        clan.leave(user);
        clanByPlayerCache.invalidate(user.getUuid());
        clanByIdCache.put(user.getUuid(), Futures.immediateFuture(clan));
        clanSystem.getUserManager().setClan(user, null);
        return repository.leaveClan(user, clan);
    }

    public ListenableFuture<Boolean> renameClan(AbstractClan clan, String name, String tag) {
        String oldName = clan.getClanName();
        String oldTag = clan.getClanTag();
        if (!oldName.equalsIgnoreCase(name)) {
            clanByNameCache.invalidate(oldName);
        }
        if (!oldTag.equalsIgnoreCase(tag)) {
            clanByTagCache.invalidate(oldTag);
        }
        clan.rename(name, tag);
        clanByNameCache.put(name.toLowerCase(), Futures.immediateFuture(clan.getClanId()));
        clanByTagCache.put(tag.toLowerCase(), Futures.immediateFuture(clan.getClanId()));
        return repository.renameClan(clan, oldName, oldTag);
    }

    public ListenableFuture<Boolean> acceptInvite(ClanSystem clanSystem, AbstractUser user, AbstractClan clan) {
        user.setClan(clan.getClanId());
        clan.join(user.getUuid());
        clanByIdCache.put(clan.getClanId(), Futures.immediateFuture(clan));
        clanByPlayerCache.put(user.getUuid(), Futures.immediateFuture(clan.getClanId()));
        clanSystem.getUserManager().setClan(user, clan.getClanId());
        clanSystem.getInviteManager().removeInvite(user.getUuid(), clan.getClanId());
        return repository.joinClan(user, clan);
    }

    public ListenableFuture<Boolean> denyInvite(ClanSystem clanSystem, AbstractUser user, AbstractClan clan) {
        return clanSystem.getInviteManager().removeInvite(user.getUuid(), clan.getClanId());
    }

    public ListenableFuture<Clan.Group> promoteUser(AbstractUser user, AbstractClan clan) {
        Clan.Group group = clan.promote(user);
        updateClan(clan);
        repository.updateMembers(clan);
        return Futures.immediateFuture(group);
    }

    public ListenableFuture<Clan.Group> demoteUser(AbstractUser user, AbstractClan clan) {
        Clan.Group group = clan.demote(user);
        updateClan(clan);
        repository.updateMembers(clan);
        return Futures.immediateFuture(group);
    }

    public ListenableFuture<Boolean> isNameAvailable(String clanName) {
        return Futures.transform(clanByNameCache.getUnchecked(clanName.toLowerCase()), Objects::isNull, Executors.asyncExecutor());
    }

    public ListenableFuture<Boolean> isTagAvailable(String clanTag) {
        return Futures.transform(clanByTagCache.getUnchecked(clanTag.toLowerCase()), Objects::isNull, Executors.asyncExecutor());
    }

    public ListenableFuture<AbstractClan> getClanById(UUID id) {
        if (id == null) {
            return Futures.immediateFuture(null);
        }
        return clanByIdCache.getUnchecked(id);
    }

    public ListenableFuture<AbstractClan> getClanByName(String clanName) {
        return Futures.transformAsync(repository.getClanByName(clanName), this::getClanById, Executors.asyncExecutor());
    }

    public ListenableFuture<AbstractClan> getClanByTag(String clanTag) {
        return Futures.transformAsync(repository.getClanByTag(clanTag), this::getClanById, Executors.asyncExecutor());
    }

    public ListenableFuture<AbstractClan> getClanByPlayer(UUID player) {
        return Futures.transformAsync(repository.getClanByPlayer(player), this::getClanById, Executors.asyncExecutor());
    }

    private void updateClan(AbstractClan clan) {
        clanByIdCache.put(clan.getClanId(), Futures.immediateFuture(clan));
    }
}
