package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.AbstractUser;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface UserRepository {

    /**
     * Inserts a user into the database.
     *
     * @param user The user to insert.
     * @return a ListenableFuture with a Boolean which returns true if the user has been inserted.
     */
    ListenableFuture<Boolean> insertUser(AbstractUser user);

    /**
     * Gets a user from the database.
     *
     * @param player The uuid of the user.
     * @return a ListenableFuture with an AbstractUser if the user could be found.
     */
    ListenableFuture<AbstractUser> getUser(UUID player);

    /**
     * Updates a users clan in the database.
     *
     * @param player The uuid of the user.
     * @param newClan The id of the new clan.
     * @return a ListenableFuture with a Boolean which returns true if the clan has been updated.
     */
    ListenableFuture<Boolean> setClan(UUID player, UUID newClan);

    /**
     * Updates a users locale in the database.
     *
     * @param user The user to update.
     * @param locale The new users' locale.
     * @return a ListenableFuture with a Boolean which returns true if the user has been updated.
     */
    ListenableFuture<Boolean> updateLocale(AbstractUser user, Locale locale);

    /**
     * Caches an uuid to the database by its name.
     *
     * @param name The name of the user.
     * @param player The uuid of the user.
     * @return a ListenableFuture with a Boolean which returns true if the uuid has been cached.
     */
    ListenableFuture<Boolean> cacheUUID(String name, UUID player);

    /**
     * Gets an uuid from the database.
     *
     * @param name The name of the user.
     * @return a ListenableFuture with the users uuid if it could be found.
     */
    ListenableFuture<UUID> getUUID(String name);
}
