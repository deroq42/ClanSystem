package de.deroq.clans.invite;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.user.AbstractClanUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.MessageBuilder;
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
public class ClanInviteManagerImplementation implements ClanInviteManager {

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

    @Override
    public ListenableFuture<Boolean> sendInvite(AbstractClanUser invited, AbstractClan clan, AbstractClanUser inviter, Set<Pair<UUID, UUID>> invites) {
        Pair<UUID, UUID> invite = Pair.of(clan.getClanId(), inviter.getUuid());
        invites.add(invite);
        inviteCache.put(invited.getUuid(), Futures.immediateFuture(invites));
        invited.sendMessage("clan-got-invited", clan.getClanName());
        invited.sendMessage(
                new MessageBuilder(invited)
                .addClickEvent("clan-invite-accept-button", "/clan join " + clan.getClanName())
                .addClickEvent("clan-invite-deny-button", "/clan deny " + clan.getClanName())
                .toComponent()
        );
        return repository.insertInvite(
                invited.getUuid(),
                clan.getClanId(),
                inviter.getUuid()
        );
    }

    @Override
    public ListenableFuture<Boolean> denyInvite(AbstractClanUser user, AbstractClan clan, Set<Pair<UUID, UUID>> invites) {
        invites.forEach(clanUuidPair -> {
            ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(clanUuidPair.getValue());
            Callback.of(userFuture, inviterUser -> {
                if (inviterUser != null) {
                    inviterUser.sendMessage("invites-invite-got-denied", user.getName());
                }
            });
        });
        return removeInvite(user, clan);
    }

    @Override
    public ListenableFuture<Boolean> denyAllInvites(AbstractClanUser user, Set<Pair<UUID, UUID>> invites) {
        invites.forEach(clanUuidPair -> {
            ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(clanUuidPair.getValue());
            Callback.of(userFuture, inviterUser -> {
                if (inviterUser != null) {
                    inviterUser.sendMessage("invites-invite-got-denied", user.getName());
                }
            });
        });
        return removeInvitesByUser(user);
    }

    @Override
    public ListenableFuture<Boolean> removeInvite(AbstractClanUser user, AbstractClan clan) {
        inviteCache.invalidate(user.getUuid());
        return repository.deleteInvite(user.getUuid(), clan.getClanId());
    }

    @Override
    public ListenableFuture<Boolean> removeInvitesByUser(AbstractClanUser user) {
        inviteCache.invalidate(user.getUuid());
        return repository.deleteInvitesByPlayer(user.getUuid());
    }

    @Override
    public ListenableFuture<Boolean> removeInvitesByClan(AbstractClan clan) {
        return repository.deleteInvitesByClan(clan.getClanId());
    }

    @Override
    public ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(AbstractClanUser user) {
        return inviteCache.getUnchecked(user.getUuid());
    }

    @Override
    public ListenableFuture<Boolean> checkForPendingInvites(AbstractClanUser user) {
        ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = getInvites(user);
        Callback.of(invitesFuture, invites -> {
            if (!invites.isEmpty()) {
                user.sendMessage("invites-pending-invites-" + (invites.size() == 1 ? "one" : "multiple"));
                user.sendMessage(new MessageBuilder(user, "invites-pending-invites-button").addClickEvent("/clan invites").toComponent());
            }
        });
        return Futures.immediateFuture(true);
    }
}
