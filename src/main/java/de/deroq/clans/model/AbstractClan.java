package de.deroq.clans.model;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.user.AbstractUser;
import lombok.Getter;

import java.util.*;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface AbstractClan {

    void rename(String name, String tag);

    void join(UUID player);

    void leave(AbstractUser user);

    Clan.Group promote(AbstractUser user);

    Clan.Group demote(AbstractUser user);

    void chat(AbstractUser user, String message);

    void broadcast(String translationKey, Object... objects);

    Clan.Group getGroup(AbstractUser user);

    boolean isLeader(AbstractUser user);

    boolean isDefault(AbstractUser user);

    boolean containsUser(AbstractUser user);

    boolean canKick(AbstractUser toKick, AbstractUser from);

    Collection<AbstractUser> getOnlinePlayers();

    Set<ListenableFuture<AbstractUser>> getMembersAsFuture();

    Set<ListenableFuture<AbstractUser>> getOnlineLeadersAsFuture();

    UUID getClanId();

    String getClanName();

    String getClanTag();

    Map<UUID, Clan.Group> getMembers();

    AbstractInfo getInfo();

    interface AbstractInfo {

        Map<Clan.Group, Set<UUID>> getMembers();

        Set<UUID> getUsersWithGroup(Clan.Group group);

        void update(UUID player, Clan.Group group);

        void remove(UUID player);
    }

    enum Group {

        LEADER(2, "Leader"),
        MODERATOR(1, "Moderator"),
        DEFAULT(0, "Mitglied");

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
