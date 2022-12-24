package de.deroq.clans.api;

import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Pair;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Miles
 * @since 23.12.2022
 */
public interface ClanAPI {

    void getClanById(UUID id, Consumer<AbstractClan> consumer);

    void getClanByName(String name, Consumer<UUID> consumer);

    void getClanByTag(String tag, Consumer<UUID> consumer);

    void getClanByPlayer(UUID player, Consumer<UUID> consumer);

    void getUser(UUID player, Consumer<AbstractClanUser> consumer);

    void getRequests(UUID clan, Consumer<Set<UUID>> consumer);

    void getInvites(UUID player, Consumer<Set<Pair<UUID, UUID>>> consumer);
}
