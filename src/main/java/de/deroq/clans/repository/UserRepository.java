package de.deroq.clans.repository;

import com.google.common.cache.Cache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.ClanUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface UserRepository {

    ListenableFuture<Boolean> insertUser(ClanUser user);

    ListenableFuture<ClanUser> getUser(UUID uuid, Cache<UUID, ListenableFuture<ClanUser>> cache);

    ListenableFuture<Boolean> setClan(UUID player, UUID newClan);

    ListenableFuture<Boolean> cacheUUID(String name, UUID uuid);

    ListenableFuture<UUID> getUUID(String name);
}
