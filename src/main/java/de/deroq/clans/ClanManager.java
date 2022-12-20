package de.deroq.clans;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.AbstractUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface ClanManager {

    ListenableFuture<AbstractClan> createClan(AbstractUser user, String clanName, String clanTag);

    ListenableFuture<Boolean> deleteClan(AbstractClan clan);

    ListenableFuture<Boolean> leaveClan(AbstractUser user, AbstractClan clan);

    ListenableFuture<Boolean> renameClan(AbstractClan clan, String name, String tag);

    ListenableFuture<Boolean> joinClan(AbstractUser user, AbstractClan clan);

    ListenableFuture<Clan.Group> promoteUser(AbstractUser user, AbstractClan clan);

    ListenableFuture<Clan.Group> demoteUser(AbstractUser user, AbstractClan clan);

    ListenableFuture<Boolean> isNameAvailable(String clanName);

    ListenableFuture<Boolean> isTagAvailable(String clanTag);

    ListenableFuture<AbstractClan> getClanById(UUID id);

    ListenableFuture<AbstractClan> getClanByName(String clanName);

    ListenableFuture<AbstractClan> getClanByTag(String clanTag);

    ListenableFuture<AbstractClan> getClanByPlayer(UUID player);

    void updateClan(AbstractClan clan);

    LoadingCache<UUID, ListenableFuture<AbstractClan>> getClanByIdCache();

    LoadingCache<String, ListenableFuture<UUID>> getClanByNameCache();

    LoadingCache<String, ListenableFuture<UUID>> getClanByTagCache();

    LoadingCache<UUID, ListenableFuture<UUID>> getClanByPlayerCache();
}
