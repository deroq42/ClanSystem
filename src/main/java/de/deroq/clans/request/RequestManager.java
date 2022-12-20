package de.deroq.clans.request;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface RequestManager {

    ListenableFuture<Boolean> sendRequest(AbstractClan clan, AbstractUser user, Set<UUID> requests);

    ListenableFuture<Boolean> acceptRequest(AbstractUser accepted, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Boolean> declineRequest(AbstractUser declined, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Boolean> removeRequest(AbstractUser user, AbstractClan clan, Set<UUID> requests);

    ListenableFuture<Set<UUID>> getRequests(AbstractClan clan);

    void checkForPendingRequests(AbstractUser user);

    LoadingCache<UUID, ListenableFuture<Set<UUID>>> getRequestCache();
}
