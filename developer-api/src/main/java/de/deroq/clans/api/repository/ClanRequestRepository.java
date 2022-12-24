package de.deroq.clans.api.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface ClanRequestRepository {

    ListenableFuture<Boolean> insertRequest(AbstractClan clan, AbstractClanUser user);

    ListenableFuture<Boolean> deleteRequest(AbstractClan clan, AbstractClanUser user);

    ListenableFuture<Set<UUID>> getRequests(UUID clan);
}
