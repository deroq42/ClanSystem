package de.deroq.clans.invite;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
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

    private final ClanInviteRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<Set<UUID>>> inviteCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<Set<UUID>>>() {
                @Override
                public ListenableFuture<Set<UUID>> load(UUID player) {
                    return repository.getInvites(player);
                }
            });

    public void sendInvite(AbstractUser invited, AbstractClan clan, AbstractUser inviter, Set<UUID> invites) {
        ListenableFuture<Boolean> future = repository.insertInvite(
                invited.getUuid(),
                clan.getClanId(),
                inviter.getUuid()
        );
        Callback.of(future, inviteSent -> {
            if (inviteSent) {
                invites.add(clan.getClanId());
                inviter.sendMessage("Du hast §c" + invited.getName() + " §7eine Einladung gesendet");
                invited.sendMessage("Du wurdest vom Clan §c" + clan.getClanName() + " §7eingeladen");
                inviteCache.put(invited.getUuid(), Futures.immediateFuture(invites));
            }
        });
    }

    public ListenableFuture<Boolean> removeInvite(UUID player, UUID clan) {
        inviteCache.invalidate(player);
        return repository.deleteInvite(
                player,
                clan
        );
    }

    public ListenableFuture<Boolean> removeInvitesByPlayer(UUID player) {
        inviteCache.invalidate(player);
        return repository.deleteInvitesByPlayer(player);
    }

    public ListenableFuture<Boolean> removeInvitesByClan(UUID clan) {
        return repository.deleteInvitesByClan(clan);
    }

    public ListenableFuture<Set<UUID>> getInvites(UUID player) {
        return inviteCache.getUnchecked(player);
    }
}
