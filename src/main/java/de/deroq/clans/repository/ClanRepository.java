package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.Clan;
import de.deroq.clans.util.Pair;

import java.util.UUID;

/**
 * @author Miles
 * @since 08.12.2022
 */
public interface ClanRepository {

    ListenableFuture<Clan> insertClan(Clan clan);

    ListenableFuture<Void> deleteClan(Clan clan);

    ListenableFuture<Pair<String, String>> renameClan(Clan clan);

    ListenableFuture<Clan> getClanById(UUID id);

    ListenableFuture<UUID> getClanByName(String clanName);

    ListenableFuture<UUID> getClanByTag(String clanTag);
}
