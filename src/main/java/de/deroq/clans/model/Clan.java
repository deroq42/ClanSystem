package de.deroq.clans.model;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.ClanUser;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class Clan {

    private final ClanSystem clanSystem;

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

    public Clan(ClanSystem clanSystem, UUID clanId, String clanName, String clanTag, Map<UUID, Clan.Group> members) {
        this.clanSystem = clanSystem;
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

    public synchronized void join(UUID player) {
        members.put(player, Clan.Group.DEFAULT);
    }

    public synchronized void leave(UUID player) {
        getMembers().remove(player);
        getInfo().remove(player);
    }

    public synchronized Clan.Group promote(UUID player) {
        Clan.Group oldGroup = members.get(player);
        Clan.Group newGroup = Clan.Group.getNextGroup(oldGroup);
        if (newGroup == null) {
            return null;
        }
        info.update(player, newGroup);
        members.replace(player, oldGroup, newGroup);
        return newGroup;
    }

    public synchronized Clan.Group demote(UUID player) {
        Clan.Group oldGroup = members.get(player);
        Clan.Group newGroup = Clan.Group.getPreviousGroup(oldGroup);
        if (newGroup == null) {
            return null;
        }
        info.update(player, newGroup);
        members.replace(player, oldGroup, newGroup);
        return newGroup;
    }

    public synchronized void broadcast(String message) {
        members.keySet()
                .stream()
                .map(uuid -> ProxyServer.getInstance().getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(player -> player.sendMessage(TextComponent.fromLegacyText(ClanSystem.PREFIX + message)));
    }

    public Clan.Group getGroup(ClanUser user) {
        return members.get(user.getUuid());
    }

    public boolean isLeader(ClanUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.LEADER;
    }

    public boolean isDefault(ClanUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.DEFAULT;
    }

    public boolean containsUser(ClanUser user) {
        return members.containsKey(user.getUuid());
    }

    public Collection<UUID> getOnlinePlayers() {
        return members.keySet()
                .stream()
                .filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null)
                .collect(Collectors.toList());
    }

    public Set<ClanUser> getUsers() {
        Set<ClanUser> users = new HashSet<>();
        members.keySet().forEach(uuid -> {
            try {
                users.add(clanSystem.getUserManager().getUser(uuid).get(50, TimeUnit.MILLISECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            }
        });
        return users;
    }

    public enum Group {

        LEADER(2, "Leader"),
        MODERATOR(1, "Moderator"),
        DEFAULT(0, "Mitglied");

        @Getter
        private final int id;

        @Getter
        private final String text;

        Group(int id, String text) {
            this.id = id;
            this.text = text;
        }

        public static Group getNextGroup(Group oldGroup) {
            for (Group group : Group.values()) {
                if (group.getId() == oldGroup.getId() + 1) {
                    return group;
                }
            }
            return null;
        }

        public static Group getPreviousGroup(Group oldGroup) {
            for (Group group : Group.values()) {
                if (group.getId() == oldGroup.getId() - 1) {
                    return group;
                }
            }
            return null;
        }
    }

    public static class Info {

        private final Clan clan;

        @Getter
        private final Map<Clan.Group, Set<UUID>> members;

        public Info(Clan clan) {
            this.clan = clan;
            this.members = new HashMap<>();
            members.put(Clan.Group.LEADER, getUsersWithGroup(Clan.Group.LEADER));
            members.put(Clan.Group.MODERATOR, getUsersWithGroup(Clan.Group.MODERATOR));
            members.put(Clan.Group.DEFAULT, getUsersWithGroup(Clan.Group.DEFAULT));
        }

        public Set<UUID> getUsersWithGroup(Clan.Group group) {
            return clan.getMembers().entrySet()
                    .stream()
                    .filter(uuidGroupEntry -> uuidGroupEntry.getValue() == group)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }

        public void update(UUID player, Clan.Group group) {
            remove(player);
            members.get(group).add(player);
        }

        public void remove(UUID player) {
            Arrays.stream(Clan.Group.values())
                    .filter(group -> members.get(group).contains(player))
                    .forEach(group -> members.get(group).remove(player));
        }
    }
}
