package de.deroq.clans.command;

import de.deroq.clans.model.Clan;
import de.deroq.clans.user.ClanUser;

import java.util.*;

/**
 * @author Miles
 * @since 10.12.2022
 */
public abstract class ClanSubCommand {

    public abstract void run(ClanUser user, String[] args);

    public void sendHelp(ClanUser user) {
        user.sendMessage("/clan create <name> <tag>");
        user.sendMessage("/clan delete");
        user.sendMessage("/clan rename <name> <tag>");
        user.sendMessage("/clan invite <player>");
        user.sendMessage("/clan join <name>");
        user.sendMessage("/clan deny <name>");
        user.sendMessage("/clan leave");
        user.sendMessage("/clan promote <player>");
        user.sendMessage("/clan demote <player>");
        user.sendMessage("/clan info");
    }

    public void sendInfo(ClanUser user, Clan clan, boolean showOnlineStatus) {
        user.sendMessage("Informationen zum " + clan.getClanName());
        user.sendMessage("Name: " + clan.getClanName());
        user.sendMessage("Tag: " + clan.getClanTag());
        user.sendMessage("Mitglieder: " + clan.getMembers().size());

        Map<Clan.Group, Set<String>> map = new HashMap<>();
        for (ClanUser target : clan.getUsers()) {
            Clan.Group group = clan.getGroup(target);
            Set<String> names = map.computeIfAbsent(group, o -> new HashSet<>());
            String text = target.getName();
            if (showOnlineStatus) {
                String onlineStatus = (target.isOnline() ? "§aOnline" : "§cOffline");
                text = text + " §7(" + onlineStatus + "§7)";
            }
            names.add(text);
        }
        // Leader
        Set<String> leaders = map.getOrDefault(Clan.Group.LEADER, Collections.emptySet());
        user.sendMessage("Leader (" + leaders.size() + ")");
        leaders.forEach(user::sendMessage);

        // Moderatoren
        Set<String> mods = map.getOrDefault(Clan.Group.MODERATOR, Collections.emptySet());
        user.sendMessage("Moderatoren (" + mods.size() + ")");
        mods.forEach(user::sendMessage);

        // Mitglieder
        Set<String> defaults = map.getOrDefault(Clan.Group.DEFAULT, Collections.emptySet());
        user.sendMessage("Mitglieder (" + defaults.size() + ")");
        defaults.forEach(user::sendMessage);
    }
}
