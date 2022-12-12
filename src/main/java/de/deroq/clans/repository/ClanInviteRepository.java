package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.util.Pair;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface ClanInviteRepository {

    /**
     * Inserts an invitation into the database.
     *
     * @param invited The uuid of the invited user.
     * @param clan The clan id.
     * @param inviter The uuid of the inviter.
     * @return a ListenableFuture with a Boolean which returns true if the invitation has been inserted.
     */
    ListenableFuture<Boolean> insertInvite(UUID invited, UUID clan, UUID inviter);

    /**
     * Deletes a pending invitation from the database by the players uuid and the clans' id.
     *
     * @param player The uuid of the user.
     * @param clan The clan id.
     * @return a ListenableFuture with a Boolean which returns true if the invitation has been deleted.
     */
    ListenableFuture<Boolean> deleteInvite(UUID player, UUID clan);

    /**
     * Deletes all pending invitation from the database by the players uuid.
     *
     * @param player The uuid of the user.
     * @return a ListenableFuture with a Boolean which returns true if the invitation has been deleted.
     */
    ListenableFuture<Boolean> deleteInvitesByPlayer(UUID player);

    /**
     * Deletes all pending invitation from the database by the clan id.
     *
     * @param clan The clan id.
     * @return a ListenableFuture with a Boolean which returns true if the invitation has been deleted.
     */
    ListenableFuture<Boolean> deleteInvitesByClan(UUID clan);

    /**
     * Gets a set of invitations from the database.
     *
     * @param player The uuid of the user.
     * @return a ListenableFuture with a set of invitations.
                                                Clan ID
                                                Inviter UUID
     */
    ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(UUID player);
}
