package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.AbstractClanUser;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface ClanUserRepository {

    ListenableFuture<Boolean> insertUser(AbstractClanUser user);

    ListenableFuture<AbstractClanUser> getUser(UUID player);

    ListenableFuture<Boolean> setClan(UUID player, UUID newClan);

    ListenableFuture<Boolean> updateLocale(AbstractClanUser user, Locale locale);

    ListenableFuture<Boolean> cacheUUID(String name, UUID player);

    ListenableFuture<UUID> getUUID(String name);
}
