package de.deroq.clans.request;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractClanUser;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface ClanRequestManager {

    ListenableFuture<Boolean> sendRequest(AbstractClan clan, AbstractClanUser user, Set<UUID> requests);

    ListenableFuture<Boolean> acceptRequest(AbstractClanUser accepted, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Boolean> declineRequest(AbstractClanUser declined, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Boolean> removeRequest(AbstractClanUser user, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Set<UUID>> getRequests(AbstractClan clan);

    ListenableFuture<Boolean> checkForPendingRequests(AbstractClanUser user);

    LoadingCache<UUID, ListenableFuture<Set<UUID>>> getRequestCache();
}
