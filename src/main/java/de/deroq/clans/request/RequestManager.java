package de.deroq.clans.request;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanRequestRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class RequestManager {

    private final ClanSystem clanSystem;
    private final ClanRequestRepository repository;

    @Getter
    private final LoadingCache<UUID, ListenableFuture<Set<UUID>>> requestCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ListenableFuture<Set<UUID>>>() {
                @Override
                public ListenableFuture<Set<UUID>> load(UUID clan) {
                    return repository.getRequests(clan);
                }
            });

    public ListenableFuture<Boolean> sendRequest(AbstractClan clan, AbstractUser user, Set<UUID> requests) {
        for (ListenableFuture<AbstractUser> userFuture : clan.getOnlineLeadersAsFuture()) {
            Callback.of(userFuture, leader -> leader.sendMessage("§c" + user.getName() + " §7hat eine Beitrittsanfrage gesendet"));
        }
        requests.add(user.getUuid());
        requestCache.put(clan.getClanId(), Futures.immediateFuture(requests));
        return repository.insertRequest(clan, user);
    }

    public ListenableFuture<Boolean> acceptRequest(AbstractUser accepted, AbstractUser from, AbstractClan clan, Set<UUID> requests) {
        for (ListenableFuture<AbstractUser> userFuture : clan.getOnlineLeadersAsFuture()) {
            Callback.of(userFuture, leader -> leader.sendMessage("§c" + from.getName() + " §7hat die Beitrittsanfrage von §c" + accepted.getName() + " §7angenommen"));
        }
        removeRequest(accepted, clan, requests);
        return clanSystem.getClanManager().joinClan(accepted, clan);
    }

    public ListenableFuture<Boolean> declineRequest(AbstractUser declined, AbstractUser from, AbstractClan clan, Set<UUID> requests) {
        for (ListenableFuture<AbstractUser> userFuture : clan.getOnlineLeadersAsFuture()) {
            Callback.of(userFuture, leader -> leader.sendMessage("§c" + from.getName() + " §7hat die Beitrittsanfrage von §c" + declined.getName() + " §7abgelehnt"));
        }
        return removeRequest(declined, clan, requests);
    }

    public ListenableFuture<Boolean> removeRequest(AbstractUser user, AbstractClan clan, Set<UUID> requests) {
        requests.remove(user.getUuid());
        requestCache.put(user.getUuid(), Futures.immediateFuture(requests));
        return repository.deleteRequest(clan, user);
    }

    public ListenableFuture<Set<UUID>> getRequests(AbstractClan clan) {
        return requestCache.getUnchecked(clan.getClanId());
    }
}
