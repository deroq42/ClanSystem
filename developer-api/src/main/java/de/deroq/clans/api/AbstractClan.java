package de.deroq.clans.api;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.user.AbstractClanUser;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Miles
 * @since 23.12.2022
 */
public interface AbstractClan {

    /**
     * Updates the clanname and clantag.
     *
     * @param name The new name of the clan.
     * @param tag The new tag of the clan.
     */
    void rename(String name, String tag);

    /**
     * Puts the player into the members map.
     *
     * @param player The player who joins the clan.
     */
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

        Map<AbstractClan.Group, Set<UUID>> getMembers();

        Set<UUID> getUsersWithGroup(AbstractClan.Group group);

        void update(UUID player, AbstractClan.Group group);

        void remove(UUID player);
    }

    enum Group {

        LEADER(2, "clan-group-leader"),
        MODERATOR(1, "clan-group-moderator"),
        DEFAULT(0, "clan-group-default");

        private final int id;
        private final String translationKey;

        Group(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public int getId() {
            return id;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public static AbstractClan.Group getNextGroup(AbstractClan.Group oldGroup) {
            for (AbstractClan.Group group : AbstractClan.Group.values()) {
                if (group.getId() == oldGroup.getId() + 1) {
                    return group;
                }
            }
            return null;
        }

        public static AbstractClan.Group getPreviousGroup(AbstractClan.Group oldGroup) {
            for (AbstractClan.Group group : AbstractClan.Group.values()) {
                if (group.getId() == oldGroup.getId() - 1) {
                    return group;
                }
            }
            return null;
        }
    }
}
