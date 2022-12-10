package de.deroq.clans.repository;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.ClanUser;

import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public interface UserRepository {

    ListenableFuture<ClanUser> insertUser(ClanUser user);

    ListenableFuture<ClanUser> getUser(UUID uuid);

    void updateClan(UUID player, UUID clan);
}
