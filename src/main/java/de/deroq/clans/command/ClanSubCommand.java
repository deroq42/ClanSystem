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

    public void sendHelp(AbstractUser user, int page) {
        user.sendMessage("clan-help-page" + page);
    }

    public void sendInfo(ClanSystem clanSystem, AbstractUser user, AbstractClan clan, boolean showOnlineStatus) {
        user.sendMessage("clan-info-header",
                clan.getClanName(),
                clan.getClanName(),
                clan.getClanTag(),
                clan.getMembers().size(), ClanSystem.CLAN_PLAYER_LIMIT
        );
        Map<Clan.Group, Map<String, String>> map = new HashMap<>();
        for (ListenableFuture<AbstractUser> future : clan.getMembersAsFuture()) {
            Callback.of(future, member -> {
                Clan.Group group = clan.getGroup(member);
                Map<String, String> names = map.computeIfAbsent(group, o -> new HashMap<>());
                String translationKey;
                if (showOnlineStatus) {
                    translationKey = (member.isOnline() ? "clan-info-user-online-format" : "clan-info-user-offline-format");
                } else {
                    translationKey = "clan-info-user-format";
                }
                names.put(translationKey, member.getName());
            });
        }
        ProxyServer.getInstance().getScheduler().schedule(clanSystem, () -> {
            // Leader
            Map<String, String> leaders = map.getOrDefault(Clan.Group.LEADER, Collections.emptyMap());
            user.sendMessage("clan-info-leaders-header", leaders.size());
            leaders.forEach(user::sendMessage);

            // Moderatoren
            Map<String, String> mods = map.getOrDefault(Clan.Group.MODERATOR, Collections.emptyMap());
            user.sendMessage("clan-info-moderators-header", mods.size());
            mods.forEach(user::sendMessage);

            // Mitglieder
            Map<String, String> defaults = map.getOrDefault(Clan.Group.DEFAULT, Collections.emptyMap());
            user.sendMessage("clan-info-members-header", defaults.size());
            defaults.forEach(user::sendMessage);
        }, 1, TimeUnit.MILLISECONDS);
    }
}
