package de.deroq.clans.model;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractUser;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class Clan implements AbstractClan {

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
    private final AbstractInfo info;

    public Clan(ClanSystem clanSystem, UUID clanId, String clanName, String clanTag, Map<UUID, Clan.Group> members) {
        this.clanSystem = clanSystem;
        this.clanId = clanId;
        this.clanName = clanName;
        this.clanTag = clanTag;
        this.members = members;
        this.info = new ClanInfo(this);
    }

    @Override
    public synchronized void rename(String name, String tag) {
        this.clanName = name;
        this.clanTag = tag;
    }

    @Override
    public synchronized void join(UUID player) {
        members.put(player, Clan.Group.DEFAULT);
    }

    @Override
    public synchronized void leave(AbstractUser user) {
        getMembers().remove(user.getUuid());
        getInfo().remove(user.getUuid());
    }

    @Override
    public synchronized Clan.Group promote(AbstractUser user) {
        Clan.Group oldGroup = members.get(user.getUuid());
        Clan.Group newGroup = Clan.Group.getNextGroup(oldGroup);
        if (newGroup == null) {
            return null;
        }
        info.update(user.getUuid(), newGroup);
        members.replace(user.getUuid(), oldGroup, newGroup);
        return newGroup;
    }

    @Override
    public synchronized Clan.Group demote(AbstractUser user) {
        Clan.Group oldGroup = members.get(user.getUuid());
        Clan.Group newGroup = Clan.Group.getPreviousGroup(oldGroup);
        if (newGroup == null) {
            return null;
        }
        info.update(user.getUuid(), newGroup);
        members.replace(user.getUuid(), oldGroup, newGroup);
        return newGroup;
    }

    @Override
    public synchronized void chat(AbstractUser user, String message) {
        broadcast("§c" + user.getName() + "§7: " + message);
    }

    @Override
    public synchronized void broadcast(String message) {
        members.keySet()
                .stream()
                .map(uuid -> ProxyServer.getInstance().getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(player -> player.sendMessage(TextComponent.fromLegacyText(ClanSystem.PREFIX + message)));
    }

    @Override
    public Clan.Group getGroup(AbstractUser user) {
        return members.get(user.getUuid());
    }

    @Override
    public boolean isLeader(AbstractUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.LEADER;
    }

    @Override
    public boolean isDefault(AbstractUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.DEFAULT;
    }

    @Override
    public boolean containsUser(AbstractUser user) {
        return members.containsKey(user.getUuid());
    }

    @Override
    public Collection<UUID> getOnlinePlayers() {
        return members.keySet()
                .stream()
                .filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null)
                .collect(Collectors.toList());
    }

    @Override
    public Set<ListenableFuture<AbstractUser>> getUsersAsFuture() {
        Set<ListenableFuture<AbstractUser>> users = new HashSet<>();
        members.keySet().forEach(uuid -> users.add(clanSystem.getUserManager().getUser(uuid)));
        return users;
    }

    static class ClanInfo implements AbstractInfo {

        private final Clan clan;

        @Getter
        private final Map<Clan.Group, Set<UUID>> members;

        public ClanInfo(Clan clan) {
            this.clan = clan;
            this.members = new HashMap<>();
            members.put(Clan.Group.LEADER, getUsersWithGroup(Clan.Group.LEADER));
            members.put(Clan.Group.MODERATOR, getUsersWithGroup(Clan.Group.MODERATOR));
            members.put(Clan.Group.DEFAULT, getUsersWithGroup(Clan.Group.DEFAULT));
        }

        @Override
        public Set<UUID> getUsersWithGroup(Clan.Group group) {
            return clan.getMembers().entrySet()
                    .stream()
                    .filter(uuidGroupEntry -> uuidGroupEntry.getValue() == group)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }

        @Override
        public void update(UUID player, Clan.Group group) {
            remove(player);
            members.get(group).add(player);
        }

        @Override
        public void remove(UUID player) {
            Arrays.stream(Clan.Group.values())
                    .filter(group -> members.get(group).contains(player))
                    .forEach(group -> members.get(group).remove(player));
        }
    }
}
