package de.deroq.clans;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.Clan;
import de.deroq.clans.repository.ClanRepository;
import de.deroq.clans.util.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 09.12.2022
 */
@RequiredArgsConstructor
public class ClanManager {

    private final ClanRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<Clan>> clanByIdCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<Clan>>() {
                @Override
                public ListenableFuture<Clan> load(UUID id) {
                    return repository.getClanById(id);
                }
            });

    @Getter
    private final LoadingCache<String, ListenableFuture<UUID>> clanByNameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(String clanName) {
                    return repository.getClanByName(clanName);
                }
            });

    @Getter
    private final LoadingCache<String, ListenableFuture<UUID>> clanByTagCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, ListenableFuture<UUID>>() {
                @Override
                public ListenableFuture<UUID> load(String clanTag) {
                    return repository.getClanByTag(clanTag);
                }
            });

    public ListenableFuture<Clan> createClan(UUID player, String clanName, String clanTag) {
        UUID id = UUID.randomUUID();
        Clan clan = new Clan(
                id,
                clanName,
                clanTag,
                Collections.singletonMap(player, Clan.Group.LEADER)
        );
        clanByIdCache.put(id, Futures.immediateFuture(clan));
        clanByNameCache.put(clanName.toLowerCase(), Futures.immediateFuture(id));
        clanByTagCache.put(clanTag.toLowerCase(), Futures.immediateFuture(id));
        return repository.insertClan(clan);
    }

    public ListenableFuture<Boolean> isClanNameAvailable(String clanName) {
        return Futures.transform(clanByNameCache.getUnchecked(clanName.toLowerCase()), Objects::isNull);
    }

    public ListenableFuture<Boolean> isClanTagAvailable(String clanTag) {
        return Futures.transform(clanByTagCache.getUnchecked(clanTag.toLowerCase()), Objects::isNull);
    }

    public ListenableFuture<Clan> getClanById(UUID id) {
        return clanByIdCache.getUnchecked(id);
    }

    public ListenableFuture<Clan> getClanByName(String clanName) {
        return Futures.transformAsync(repository.getClanByName(clanName.toLowerCase()), this::getClanById, Executors.asyncExecutor());
    }

    public ListenableFuture<Clan> getClanByTag(String clanTag) {
        return Futures.transformAsync(repository.getClanByTag(clanTag.toLowerCase()), this::getClanById, Executors.asyncExecutor());
    }
}
