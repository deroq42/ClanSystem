package de.deroq.clans.command;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import net.md_5.bungee.api.ProxyServer;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles
 * @since 10.12.2022
 */
public abstract class ClanSubCommand {

    public abstract void run(AbstractUser user, String[] args);

    public void sendHelp(AbstractUser user) {
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
        user.sendMessage("/clan tinfo <tag>");
        user.sendMessage("/clan ninfo <name>");
        user.sendMessage("/clan uinfo <player>");
        user.sendMessage("/clan denyAll");
        user.sendMessage("/clan request <clan>");
        user.sendMessage("/clan accept <player>");
        user.sendMessage("/clan decline <player>");
        user.sendMessage("/clan acceptAll");
        user.sendMessage("/clan declineAll");
        user.sendMessage("/clan kick <player>");
        user.sendMessage("/cc <message>");
    }

    public void sendInfo(ClanSystem clanSystem, AbstractUser user, AbstractClan clan, boolean showOnlineStatus) {
        user.sendMessage("Informationen zum " + clan.getClanName());
        user.sendMessage("Name: " + clan.getClanName());
        user.sendMessage("Tag: " + clan.getClanTag());
        user.sendMessage("Mitglieder: " + clan.getMembers().size());

        Map<Clan.Group, Set<String>> map = new HashMap<>();
        for (ListenableFuture<AbstractUser> future : clan.getMembersAsFuture()) {
            Callback.of(future, member -> {
                Clan.Group group = clan.getGroup(member);
                Set<String> names = map.computeIfAbsent(group, o -> new HashSet<>());
                String text = member.getName();
                if (showOnlineStatus) {
                    String onlineStatus = (member.isOnline() ? "§aOnline" : "§cOffline");
                    text = text + " §7(" + onlineStatus + "§7)";
                }
                names.add(text);
            });
        }
        ProxyServer.getInstance().getScheduler().schedule(clanSystem, () -> {
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
        }, 1, TimeUnit.MILLISECONDS);
    }
}
