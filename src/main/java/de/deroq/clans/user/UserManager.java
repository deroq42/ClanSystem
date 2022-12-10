package de.deroq.clans.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class UserManager {

    private final UserRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<ClanUser>> userCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<ClanUser>>() {
                @Override
                public ListenableFuture<ClanUser> load(UUID uuid) {
                    return repository.getUser(uuid);
                }
            });

    public ListenableFuture<ClanUser> createUser(UUID uuid, String name) {
        ClanUser user = new ClanUser(
                uuid,
                name,
                null
        );
        userCache.put(uuid, Futures.immediateFuture(user));
        return repository.insertUser(user);
    }

    public ListenableFuture<ClanUser> getUser(UUID player) {
        return userCache.getUnchecked(player);
    }
}
