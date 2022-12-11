package de.deroq.clans.model;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class Clan {

    @Getter
    private final UUID clanId;

    @Getter
    private String clanName;

    @Getter
    private String clanTag;

    @Getter
    private final Map<UUID, Clan.Group> members;

    @Getter
    private final Clan.Info info;

    public Clan(UUID clanId, String clanName, String clanTag, Map<UUID, Clan.Group> members) {
        this.clanId = clanId;
        this.clanName = clanName;
        this.clanTag = clanTag;
        this.members = members;
        this.info = new Info(this);
    }

    public synchronized void rename(String name, String tag) {
        this.clanName = name;
        this.clanTag = tag;
    }

    public synchronized void delete(ClanSystem clanSystem) {
        getOnlinePlayers().forEach(uuid -> {
            ListenableFuture<ClanUser> userFuture = clanSystem.getUserManager().getUser(uuid);
            Callback.of(userFuture, onlineUser -> clanSystem.getUserManager().setClan(onlineUser, null));
        });
    }

    public void broadcast(String message) {
        members.keySet()
                .stream()
                .map(uuid -> ProxyServer.getInstance().getPlayer(uuid))
                .forEach(player -> player.sendMessage(TextComponent.fromLegacyText("§7[§cClans§7] " + message)));
    }

    public boolean isLeader(UUID player) {
        if (!members.containsKey(player)) {
            return false;
        }
        return members.get(player) == Group.LEADER;
    }

    public boolean isDefault(UUID player) {
        if (!members.containsKey(player)) {
            return false;
        }
        return members.get(player) == Group.DEFAULT;
    }

    public void setRank(UUID player, Clan.Group group) {
        members.put(player, group);
    }

    public boolean containsUser(UUID player) {
        return members.containsKey(player);
    }

    public Collection<UUID> getOnlinePlayers() {
        return members.keySet()
                .stream()
                .filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null)
                .collect(Collectors.toList());
    }

    public enum Group {

        LEADER,
        MODERATOR,
        DEFAULT;
    }

    public static class Info {

        private final Clan clan;

        @Getter
        private final Set<UUID> leaders;

        @Getter
        private final Set<UUID> moderators;

        @Getter
        private final Set<UUID> defaults;

        public Info(Clan clan) {
            this.clan = clan;
            this.leaders = getUsersWithGroup(Group.LEADER);
            this.moderators = getUsersWithGroup(Group.MODERATOR);
            this.defaults = getUsersWithGroup(Group.DEFAULT);
        }

        private Set<UUID> getUsersWithGroup(Clan.Group group) {
            return clan.getMembers().entrySet()
                    .stream()
                    .filter(uuidGroupEntry -> uuidGroupEntry.getValue() == group)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
    }

}
