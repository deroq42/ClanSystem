package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.util.Pair;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface ClanInviteRepository {

    ListenableFuture<Boolean> insertInvite(UUID invited, UUID clan, UUID inviter);

    ListenableFuture<Boolean> deleteInvite(UUID player, UUID clan);

    ListenableFuture<Boolean> deleteInvitesByPlayer(UUID player);

    ListenableFuture<Boolean> deleteInvitesByClan(UUID clan);

    ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(UUID player);
}
