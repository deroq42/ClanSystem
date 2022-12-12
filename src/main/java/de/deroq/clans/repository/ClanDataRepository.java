package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 08.12.2022
 */
public interface ClanDataRepository {

    /**
     * Inserts a clan into the database.
     *
     * @param player The uuid of the user.
     * @param clan The clan to insert.
     * @return a ListenableFuture with the created clan if it could be inserted.
     */
    ListenableFuture<AbstractClan> createClan(UUID player, AbstractClan clan);

    /**
     * Deletes a clan from the database.
     *
     * @param clan The clan to delete.
     * @return a ListenableFuture with a Boolean which returns true if the clan has been deleted.
     */
    ListenableFuture<Boolean> deleteClan(AbstractClan clan);

    /**
     * Updates the name and tag of a clan in the database.
     *
     * @param clan The clan to rename.
     * @param oldName The old name of the clan.
     * @param oldTag The old tag of the clan.
     * @return a ListenableFuture with a Boolean which returns true if the clan has been renamed.
     */
    ListenableFuture<Boolean> renameClan(AbstractClan clan, String oldName, String oldTag);

    /**
     * Updates all tables of the clan data when joining a clan.
     *
     * @param user The user who joins the clan.
     * @param clan The joined clan.
     * @return a ListenableFuture with a Boolean which returns true if everything has been updated.
     */
    ListenableFuture<Boolean> joinClan(AbstractUser user, AbstractClan clan);

    /**
     * Updates all tables of the clan data when leaving a clan.
     *
     * @param user The user who leaves the clan.
     * @param clan The left clan.
     * @return a ListenableFuture with a Boolean which returns true if everything has been updated.
     */
    ListenableFuture<Boolean> leaveClan(AbstractUser user, AbstractClan clan);

    /**
     * Updates the members of a clan in the database.
     *
     * @param clan The clan of the members.
     * @return a ListenableFuture with a Boolean which returns true if the members has been updated.
     */
    ListenableFuture<Boolean> updateMembers(AbstractClan clan);

    /**
     * Gets a clan from the database.
     *
     * @param id The id of the clan.
     * @return a ListenableFuture with an AbstractClan if the clan could be found.
     */
    ListenableFuture<AbstractClan> getClanById(UUID id);

    /**
     * Gets a clan id by its name.
     *
     * @param name The name of the clan.
     * @return a ListenableFuture with the clans id if it could be found.
     */
    ListenableFuture<UUID> getClanByName(String name);

    /**
     * Gets a clan id by its tag.
     *
     * @param tag The tag of the Clan
     * @return a ListenableFuture with the clans id if it could be found.
     */
    ListenableFuture<UUID> getClanByTag(String tag);

    /**
     * Gets a clan id by a member.
     *
     * @param player The uuid of the member.
     * @return a ListenableFuture with the clans id if it could be found.
     */
    ListenableFuture<UUID> getClanByPlayer(UUID player);
}
