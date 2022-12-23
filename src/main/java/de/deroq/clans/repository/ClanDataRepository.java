package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractClanUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 08.12.2022
 */
public interface ClanDataRepository {

    ListenableFuture<AbstractClan> createClan(UUID player, AbstractClan clan);

    ListenableFuture<Boolean> deleteClan(AbstractClan clan);

    ListenableFuture<Boolean> renameClan(AbstractClan clan, String oldName, String oldTag);

    ListenableFuture<Boolean> joinClan(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<Boolean> leaveClan(AbstractClanUser user, AbstractClan clan);

    ListenableFuture<Boolean> updateMembers(AbstractClan clan);

    ListenableFuture<AbstractClan> getClanById(UUID id);

    ListenableFuture<UUID> getClanByName(String name);

    ListenableFuture<UUID> getClanByTag(String tag);

    ListenableFuture<UUID> getClanByPlayer(UUID player);
}
