package de.deroq.clans.api;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.user.AbstractClanUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 23.12.2022
 */
public interface ClanManager {

    ListenableFuture<AbstractClan> createClan(AbstractClanUser user, String clanName, String clanTag);

    ListenableFuture<Boolean> deleteClan(AbstractClan clan);

    ListenableFuture<Boolean> leaveClan(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<Boolean> renameClan(AbstractClan clan, String name, String tag);

    ListenableFuture<Boolean> joinClan(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<AbstractClan.Group> promoteUser(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<AbstractClan.Group> demoteUser(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<Boolean> isNameAvailable(String clanName);

    ListenableFuture<Boolean> isTagAvailable(String clanTag);

    ListenableFuture<AbstractClan> getClanById(UUID id);

    ListenableFuture<AbstractClan> getClanByName(String clanName);

    ListenableFuture<AbstractClan> getClanByTag(String clanTag);

    ListenableFuture<AbstractClan> getClanByPlayer(UUID player);

    ListenableFuture<Boolean> updateClan(AbstractClan clan);

    LoadingCache<UUID, ListenableFuture<AbstractClan>> getClanByIdCache();

    LoadingCache<String, ListenableFuture<UUID>> getClanByNameCache();

    LoadingCache<String, ListenableFuture<UUID>> getClanByTagCache();

    LoadingCache<UUID, ListenableFuture<UUID>> getClanByPlayerCache();
}
