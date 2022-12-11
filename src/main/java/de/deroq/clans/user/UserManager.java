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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class UserManager {

    private final UserRepository repository;

    @Getter
    private final Map<UUID, ClanUser> onlineUserCache = new ConcurrentHashMap<>();

    @Getter
    private final LoadingCache<UUID, ListenableFuture<ClanUser>> userCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<ClanUser>>() {
                @Override
                public ListenableFuture<ClanUser> load(UUID uuid) {
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

    public ListenableFuture<Boolean> createUser(ClanSystem clanSystem, UUID uuid, String name) {
        ClanUser user = new ClanUser(
                clanSystem,
                uuid,
                name,
                null
        );
        return repository.insertUser(user);
    }

    public ListenableFuture<Boolean> setClan(ClanUser user, UUID newClan) {
        user.setClan(newClan);
        userCache.put(user.getUuid(), Futures.immediateFuture(user));
        return repository.setClan(
                user.getUuid(),
                newClan
        );
    }

    public ListenableFuture<ClanUser> getUser(UUID player) {
        if (onlineUserCache.containsKey(player)) {
            return Futures.immediateFuture(onlineUserCache.get(player));
        }
        return userCache.getUnchecked(player);
    }

    public ClanUser getOnlineUser(UUID player) {
        return onlineUserCache.get(player);
    }

    public void cacheOnlineUser(ClanUser user) {
        onlineUserCache.put(user.getUuid(), user);
    }

    public void invalidateOnlineUser(UUID player) {
        onlineUserCache.remove(player);
    }

    public ListenableFuture<Boolean> cacheUuid(String name, UUID uuid) {
        uuidCache.put(name.toLowerCase(), Futures.immediateFuture(uuid));
        return repository.cacheUUID(
                name,
                uuid
        );
    }

    public ListenableFuture<UUID> getUUID(String name) {
        return uuidCache.getUnchecked(name.toLowerCase());
    }
}
