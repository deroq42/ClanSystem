package de.deroq.clans.invite;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Pair;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface InviteManager {

    ListenableFuture<Boolean> sendInvite(AbstractUser invited, AbstractClan clan, AbstractUser from, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> denyInvite(AbstractUser user, AbstractClan clan, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> denyAllInvites(AbstractUser user, Set<Pair<UUID, UUID>> invites);

    ListenableFuture<Boolean> removeInvite(AbstractUser user, AbstractClan clan);

    ListenableFuture<Boolean> removeInvitesByUser(AbstractUser user);

    ListenableFuture<Boolean> removeInvitesByClan(AbstractClan clan);

    ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(AbstractUser user);

    ListenableFuture<Boolean> checkForPendingInvites(AbstractUser user);

    LoadingCache<UUID, ListenableFuture<Set<Pair<UUID, UUID>>>> getInviteCache();
}
