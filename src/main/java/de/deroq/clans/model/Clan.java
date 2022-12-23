package de.deroq.clans.model;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractClanUser;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

import java.util.*;
import java.util.function.Consumer;
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
    public synchronized void leave(AbstractClanUser user) {
        getMembers().remove(user.getUuid());
        getInfo().remove(user.getUuid());
    }

    @Override
    public synchronized Clan.Group promote(AbstractClanUser user) {
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
    public synchronized Clan.Group demote(AbstractClanUser user) {
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
    public synchronized void chat(AbstractClanUser user, String message) {
        broadcast("clan-chat-format", user.getName(), message);
    }

    @Override
    public synchronized void broadcast(String translationKey, Object... objects) {
        members.keySet()
                .stream()
                .map(uuid -> clanSystem.getUserManager().getOnlineUser(uuid))
                .forEach(user -> user.sendMessage(translationKey, objects));
    }

    @Override
    public void broadcast(Consumer<AbstractClanUser> consumer) {
        members.keySet()
                .stream()
                .map(uuid -> clanSystem.getUserManager().getOnlineUser(uuid))
                .forEach(consumer);
    }

    @Override
    public Clan.Group getGroup(AbstractClanUser user) {
        return members.get(user.getUuid());
    }

    @Override
    public boolean isLeader(AbstractClanUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.LEADER;
    }

    @Override
    public boolean isDefault(AbstractClanUser user) {
        if (!members.containsKey(user.getUuid())) {
            return false;
        }
        return members.get(user.getUuid()) == Clan.Group.DEFAULT;
    }

    @Override
    public boolean containsUser(AbstractClanUser user) {
        return members.containsKey(user.getUuid());
    }

    @Override
    public boolean canKick(AbstractClanUser toKick, AbstractClanUser from) {
        return getGroup(toKick).getId() < getGroup(from).getId();
    }

    @Override
    public Collection<AbstractClanUser> getOnlinePlayers() {
        return members.keySet()
                .stream()
                .filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null)
                .map(uuid -> clanSystem.getUserManager().getOnlineUser(uuid))
                .collect(Collectors.toList());
    }

    @Override
    public Set<ListenableFuture<AbstractClanUser>> getMembersAsFuture() {
        Set<ListenableFuture<AbstractClanUser>> users = new HashSet<>();
        members.keySet().forEach(uuid -> users.add(clanSystem.getUserManager().getUser(uuid)));
        return users;
    }

    @Override
    public Set<ListenableFuture<AbstractClanUser>> getOnlineLeadersAsFuture() {
        Set<ListenableFuture<AbstractClanUser>> users = new HashSet<>();
        members.entrySet()
                .stream()
                .filter(uuidGroupEntry -> uuidGroupEntry.getValue() == Group.LEADER)
                .map(Map.Entry::getKey)
                .filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null)
                .forEach(uuid -> users.add(clanSystem.getUserManager().getUser(uuid)));
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
