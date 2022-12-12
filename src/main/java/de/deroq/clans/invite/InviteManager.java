package de.deroq.clans.invite;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class InviteManager {

    private final ClanSystem clanSystem;
    private final ClanInviteRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<Set<Pair<UUID, UUID>>>> inviteCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<Set<Pair<UUID, UUID>>>>() {
                @Override
                public ListenableFuture<Set<Pair<UUID, UUID>>> load(UUID player) {
                    return repository.getInvites(player);
                }
            });

    public ListenableFuture<Boolean> sendInvite(AbstractUser invited, AbstractClan clan, AbstractUser inviter, Set<Pair<UUID, UUID>> invites) {
        Pair<UUID, UUID> invite = Pair.of(clan.getClanId(), inviter.getUuid());
        invites.add(invite);
        inviteCache.put(invited.getUuid(), Futures.immediateFuture(invites));
        invited.sendMessage("Du wurdest vom Clan §c" + clan.getClanName() + " §7eingeladen");
        return repository.insertInvite(
                invited.getUuid(),
                clan.getClanId(),
                inviter.getUuid()
        );
    }

    public ListenableFuture<Boolean> denyInvite(AbstractUser user, AbstractClan clan, Set<Pair<UUID, UUID>> invites) {
        invites.forEach(clanUuidPair -> {
            ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(clanUuidPair.getValue());
            Callback.of(userFuture, inviterUser -> {
                if (inviterUser != null) {
                    inviterUser.sendMessage("§c" + user.getName() + " §7hat deine Einladung abgelehnt");
                }
            });
        });
        return removeInvite(user, clan);
    }

    public ListenableFuture<Boolean> denyAllInvites(AbstractUser user, Set<Pair<UUID, UUID>> invites) {
        invites.forEach(clanUuidPair -> {
            ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(clanUuidPair.getValue());
            Callback.of(userFuture, inviterUser -> {
                if (inviterUser != null) {
                    inviterUser.sendMessage("§c" + user.getName() + " §7hat deine Einladung abgelehnt");
                }
            });
        });
        return removeInvitesByUser(user);
    }

    public ListenableFuture<Boolean> removeInvite(AbstractUser user, AbstractClan clan) {
        inviteCache.invalidate(user.getUuid());
        return repository.deleteInvite(user.getUuid(), clan.getClanId());
    }

    public ListenableFuture<Boolean> removeInvitesByUser(AbstractUser user) {
        inviteCache.invalidate(user.getUuid());
        return repository.deleteInvitesByPlayer(user.getUuid());
    }

    public ListenableFuture<Boolean> removeInvitesByClan(UUID clan) {
        return repository.deleteInvitesByClan(clan);
    }

    public ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(UUID player) {
        return inviteCache.getUnchecked(player);
    }
}
