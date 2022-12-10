package de.deroq.clans.model;

import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 09.12.2022
 */
public class Clan {

    @Getter
    private final UUID clanId;

    @Getter
    private final String clanName;

    @Getter
    private final String clanTag;

    @Getter
    private final Map<UUID, Clan.Group> members;

    public Clan(UUID clanId, String clanName, String clanTag, Map<UUID, Clan.Group> members) {
        this.clanId = clanId;
        this.clanName = clanName;
        this.clanTag = clanTag;
        this.members = members;
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
