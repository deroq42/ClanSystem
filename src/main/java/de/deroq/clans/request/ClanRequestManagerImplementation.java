package de.deroq.clans.request;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanRequestRepository;
import de.deroq.clans.user.AbstractClanUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.MessageBuilder;
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
public class ClanRequestManagerImplementation implements ClanRequestManager {

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

    public ListenableFuture<Boolean> sendRequest(AbstractClan clan, AbstractClanUser user, Set<UUID> requests) {
        for (ListenableFuture<AbstractClanUser> userFuture : clan.getOnlineLeadersAsFuture()) {
            Callback.of(userFuture, leader -> leader.sendMessage("§c" + user.getName() + " §7hat eine Beitrittsanfrage gesendet"));
        }
        requests.add(user.getUuid());
        requestCache.put(clan.getClanId(), Futures.immediateFuture(requests));
        return repository.insertRequest(clan, user);
    }

    public ListenableFuture<Boolean> acceptRequest(AbstractClanUser accepted, AbstractClan clan, Set<UUID> requests) {
        removeRequest(accepted, clan, requests);
        return clanSystem.getClanManager().joinClan(accepted, clan);
    }

    public ListenableFuture<Boolean> declineRequest(AbstractClanUser declined, AbstractClan clan, Set<UUID> requests) {
        return removeRequest(declined, clan, requests);
    }

    public ListenableFuture<Boolean> removeRequest(AbstractClanUser user, AbstractClan clan, Set<UUID> requests) {
        requests.remove(user.getUuid());
        requestCache.put(user.getUuid(), Futures.immediateFuture(requests));
        return repository.deleteRequest(clan, user);
    }

    public ListenableFuture<Set<UUID>> getRequests(AbstractClan clan) {
        return requestCache.getUnchecked(clan.getClanId());
    }

    @Override
    public ListenableFuture<Boolean> checkForPendingRequests(AbstractClanUser user) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null && currentClan.isLeader(user)) {
                ListenableFuture<Set<UUID>> requestFuture = getRequests(currentClan);
                Callback.of(requestFuture, requests -> {
                    if (!requests.isEmpty()) {
                        user.sendMessage("requests-pending-requests-" + (requests.size() == 1 ? "one" : "multiple"));
                        user.sendMessage(new MessageBuilder(user, "requests-pending-requests-button").addClickEvent("/clan requests").toComponent());
                    }
                });
            }
        });
        return Futures.immediateFuture(true);
    }
}
