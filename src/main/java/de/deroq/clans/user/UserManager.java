package de.deroq.clans.user;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface UserManager {

    ListenableFuture<Boolean> createUser(UUID uuid, String name);

    ListenableFuture<Boolean> setClan(AbstractUser user, UUID newClan);

    ListenableFuture<AbstractUser> getUser(UUID player);

    AbstractUser getOnlineUser(UUID player);

    ListenableFuture<Boolean> updateLocale(AbstractUser user, Locale locale);

    ListenableFuture<Boolean> cacheOnlineUser(AbstractUser user);

    ListenableFuture<Boolean> invalidateOnlineUser(UUID player);

    ListenableFuture<Boolean> cacheUuid(String name, UUID uuid);

    ListenableFuture<UUID> getUUID(String name);

    Map<UUID, AbstractUser> getOnlineUserCache();

    LoadingCache<UUID, ListenableFuture<AbstractUser>> getUserCache();

    LoadingCache<String, ListenableFuture<UUID>> getUuidCache();
}
