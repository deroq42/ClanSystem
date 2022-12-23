package de.deroq.clans.model;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.AbstractClanUser;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface AbstractClan {

    void rename(String name, String tag);

    void join(UUID player);

    void leave(AbstractClanUser user);

    AbstractClan.Group promote(AbstractClanUser user);

    AbstractClan.Group demote(AbstractClanUser user);

    void chat(AbstractClanUser user, String message);

    void broadcast(String translationKey, Object... objects);

    void broadcast(Consumer<AbstractClanUser> consumer);

    AbstractClan.Group getGroup(AbstractClanUser user);

    boolean isLeader(AbstractClanUser user);

    boolean isDefault(AbstractClanUser user);

    boolean containsUser(AbstractClanUser user);

    boolean canKick(AbstractClanUser toKick, AbstractClanUser from);

    Collection<AbstractClanUser> getOnlinePlayers();

    Set<ListenableFuture<AbstractClanUser>> getMembersAsFuture();

    Set<ListenableFuture<AbstractClanUser>> getOnlineLeadersAsFuture();

    UUID getClanId();

    String getClanName();

    String getClanTag();

    Map<UUID, AbstractClan.Group> getMembers();

    AbstractInfo getInfo();

    interface AbstractInfo {

        Map<Clan.Group, Set<UUID>> getMembers();

        Set<UUID> getUsersWithGroup(Clan.Group group);

        void update(UUID player, Clan.Group group);

        void remove(UUID player);
    }

    enum Group {

        LEADER(2, "clan-group-leader"),
        MODERATOR(1, "clan-group-moderator"),
        DEFAULT(0, "clan-group-default");

        @Getter
        private final int id;

        @Getter
        private final String translationKey;

        Group(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public static Clan.Group getNextGroup(Clan.Group oldGroup) {
            for (Clan.Group group : Clan.Group.values()) {
                if (group.getId() == oldGroup.getId() + 1) {
                    return group;
                }
            }
            return null;
        }

        public static Clan.Group getPreviousGroup(Clan.Group oldGroup) {
            for (Clan.Group group : Clan.Group.values()) {
                if (group.getId() == oldGroup.getId() - 1) {
                    return group;
                }
            }
            return null;
        }
    }
}
