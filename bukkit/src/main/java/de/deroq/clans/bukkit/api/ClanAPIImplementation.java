package de.deroq.clans.bukkit.api;

import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.ClanAPI;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.api.util.Pair;
import de.deroq.clans.bukkit.ClanSystem;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Miles
 * @since 23.12.2022
 */
@RequiredArgsConstructor
public class ClanAPIImplementation implements ClanAPI {

    private final ClanSystem clanSystem;

    @Override
    public void getClanById(UUID id, Consumer<AbstractClan> consumer) {
        Callback.of(clanSystem.getDataRepository().getClanById(id), consumer);
    }

    @Override
    public void getClanByName(String name, Consumer<UUID> consumer) {
        Callback.of(clanSystem.getDataRepository().getClanByName(name), consumer);
    }

    @Override
    public void getClanByTag(String tag, Consumer<UUID> consumer) {
        Callback.of(clanSystem.getDataRepository().getClanByTag(tag), consumer);
    }

    @Override
    public void getClanByPlayer(UUID player, Consumer<UUID> consumer) {
        Callback.of(clanSystem.getDataRepository().getClanByPlayer(player), consumer);
    }

    @Override
    public void getUser(UUID player, Consumer<AbstractClanUser> consumer) {
        Callback.of(clanSystem.getUserRepository().getUser(player), consumer);
    }

    @Override
    public void getRequests(UUID clan, Consumer<Set<UUID>> consumer) {
        Callback.of(clanSystem.getRequestRepository().getRequests(clan), consumer);
    }

    @Override
    public void getInvites(UUID player, Consumer<Set<Pair<UUID, UUID>>> consumer) {
        Callback.of(clanSystem.getInviteRepository().getInvites(player), consumer);
    }
}
