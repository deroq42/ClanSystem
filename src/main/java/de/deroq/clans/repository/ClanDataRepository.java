package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.ClanUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 08.12.2022
 */
public interface ClanDataRepository {

    ListenableFuture<Clan> createClan(UUID player, Clan clan);

    ListenableFuture<Boolean> deleteClan(Clan clan);

    ListenableFuture<Boolean> renameClan(Clan clan, String oldName, String oldTag);

    ListenableFuture<Boolean> joinClan(ClanUser user, Clan clan);

    ListenableFuture<Boolean> leaveClan(ClanUser user, Clan clan);

    ListenableFuture<Boolean> updateMembers(Clan clan);

    ListenableFuture<Clan> getClanById(UUID id);

    ListenableFuture<UUID> getClanByName(String clanName);

    ListenableFuture<UUID> getClanByTag(String clanTag);

    ListenableFuture<UUID> getClanByPlayer(UUID player);
}
