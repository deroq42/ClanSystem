package de.deroq.clans.api.user;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * @author Miles
 * @since 23.12.2022
 */
public interface ClanUserManager {

    ListenableFuture<Boolean> createUser(UUID uuid, String name);

    ListenableFuture<Boolean> setClan(AbstractClanUser user, UUID newClan);

    ListenableFuture<AbstractClanUser> getUser(UUID player);

    AbstractClanUser getOnlineUser(UUID player);

    ListenableFuture<Boolean> updateLocale(AbstractClanUser user, Locale locale);

    ListenableFuture<Boolean> cacheOnlineUser(AbstractClanUser user);

    ListenableFuture<Boolean> invalidateOnlineUser(UUID player);

    ListenableFuture<Boolean> cacheUuid(String name, UUID uuid);

    ListenableFuture<UUID> getUUID(String name);

    Map<UUID, AbstractClanUser> getOnlineUserCache();

    LoadingCache<UUID, ListenableFuture<AbstractClanUser>> getUserCache();

    LoadingCache<String, ListenableFuture<UUID>> getUuidCache();
}
