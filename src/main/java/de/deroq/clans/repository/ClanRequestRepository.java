package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface ClanRequestRepository {

    /**
     * Inserts a request into the database.
     *
     * @param clan The requested clan.
     * @param user The user who sends a request.
     * @return a ListenableFuture with a Boolean which returns true if the request has been inserted.
     */
    ListenableFuture<Boolean> insertRequest(AbstractClan clan, AbstractUser user);

    /**
     * Deletes a request from the database.
     *
     * @param clan The requested clan.
     * @param user The user who sent a request.
     * @return a ListenableFuture with a Boolean which returns true if the request has been deleted.
     */
    ListenableFuture<Boolean> deleteRequest(AbstractClan clan, AbstractUser user);

    /**
     * Deletes all requests from the database.
     *
     * @param clan The clan of the requests which should be deleted.
     * @return a ListenableFuture with a Boolean which returns true if the requests has been deleted.
     */
    ListenableFuture<Boolean> deleteRequests(AbstractClan clan);

    /**
     * Gets a set of requests from the database.
     *
     * @param clan The clan id.
     * @return a ListenableFuture with a set of player uuids.
     */
    ListenableFuture<Set<UUID>> getRequests(UUID clan);
}
