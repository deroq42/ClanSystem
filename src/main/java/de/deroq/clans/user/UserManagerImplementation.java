package de.deroq.clans.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class UserManagerImplementation implements UserManager {

    private final ClanSystem clanSystem;
    private final UserRepository repository;

    @Getter
    private final Map<UUID, AbstractUser> onlineUserCache = new ConcurrentHashMap<>();

    @Getter
    private final LoadingCache<UUID, ListenableFuture<AbstractUser>> userCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<AbstractUser>>() {
                @Override
                public ListenableFuture<AbstractUser> load(UUID uuid) {
                    return repository.getUser(uuid);
                }
            });

    @Getter
    private final LoadingCache<String, ListenableFuture<UUID>> uuidCache = CacheBuilder.newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build(new CacheLoader<String, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(String name) {
                    return repository.getUUID(name);
                }
            });

    @Override
    public ListenableFuture<Boolean> createUser(UUID uuid, String name) {
        ClanUser user = new ClanUser(clanSystem, uuid, name, null, Locale.forLanguageTag("de-DE"));
        userCache.put(uuid, Futures.immediateFuture(user));
        onlineUserCache.put(uuid, user);
        return repository.insertUser(user);
    }

    @Override
    public ListenableFuture<Boolean> setClan(AbstractUser user, UUID newClan) {
        user.setClan(newClan);
        userCache.put(user.getUuid(), Futures.immediateFuture(user));
        return repository.setClan(user.getUuid(), newClan);
    }

    @Override
    public ListenableFuture<AbstractUser> getUser(UUID player) {
        if (onlineUserCache.containsKey(player)) {
            return Futures.immediateFuture(onlineUserCache.get(player));
        }
        return userCache.getUnchecked(player);
    }

    @Override
    public AbstractUser getOnlineUser(UUID player) {
        return onlineUserCache.get(player);
    }

    @Override
    public ListenableFuture<Boolean> updateLocale(AbstractUser user, Locale locale) {
        user.setLocale(locale);
        return repository.updateLocale(user, locale);
    }

    @Override
    public void cacheOnlineUser(AbstractUser user) {
        onlineUserCache.put(user.getUuid(), user);
    }

    @Override
    public void invalidateOnlineUser(UUID player) {
        onlineUserCache.remove(player);
    }

    @Override
    public ListenableFuture<Boolean> cacheUuid(String name, UUID uuid) {
        uuidCache.put(name.toLowerCase(), Futures.immediateFuture(uuid));
        return repository.cacheUUID(name, uuid);
    }

    @Override
    public ListenableFuture<UUID> getUUID(String name) {
        return uuidCache.getUnchecked(name.toLowerCase());
    }
}
