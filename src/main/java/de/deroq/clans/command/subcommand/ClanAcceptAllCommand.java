package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanAcceptAllCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (!currentClan.isLeader(user)) {
                user.sendMessage("Du bist kein Leader dieses Clans");
                return;
            }
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                if (requests.isEmpty()) {
                    user.sendMessage("Es sind keine Beitrittsanfragen offen");
                    return;
                }
                if (currentClan.getMembers().size() + requests.size() >= ClanSystem.CLAN_PLAYER_LIMIT) {
                    user.sendMessage("Es ist nicht genug Platz um alle Beitrittsanfragen anzunehmen");
                    return;
                }
                requests.stream()
                        .map(uuid -> clanSystem.getUserManager().getUser(uuid))
                        .forEach(userFuture -> {
                            Callback.of(userFuture, requestedUser -> {
                                Callback.of(requestedUser.getClan(), clan -> {
                                    if (clan == null) {
                                        ListenableFuture<Boolean> acceptFuture = clanSystem.getRequestManager().acceptRequest(requestedUser, user, currentClan, requests);
                                        Callback.of(acceptFuture, accepted -> {
                                            if (accepted) {
                                                currentClan.broadcast("§c" + requestedUser.getName() + " §7hat den Clan betreten");
                                            }
                                        });
                                    } else {
                                        user.sendMessage("Dieser Spieler ist bereits in einem Clan");
                                    }
                                });
                            });
                        });
            });
            user.sendMessage("Du hast alle Beitrittsanfragen angenommen");
        });
    }
}
