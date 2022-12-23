package de.deroq.clans.invite;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractClanUser;
import de.deroq.clans.util.Pair;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface ClanInviteManager {

    ListenableFuture<Boolean> sendInvite(AbstractClanUser invited, AbstractClan clan, AbstractClanUser from, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> denyInvite(AbstractClanUser user, AbstractClan clan, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> denyAllInvites(AbstractClanUser user, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> removeInvite(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<Boolean> removeInvitesByUser(AbstractClanUser user);

    ListenableFuture<Boolean> removeInvitesByClan(AbstractClan clan);

    ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(AbstractClanUser user);

    ListenableFuture<Boolean> checkForPendingInvites(AbstractClanUser user);

    LoadingCache<UUID, ListenableFuture<Set<Pair<UUID, UUID>>>> getInviteCache();
}
