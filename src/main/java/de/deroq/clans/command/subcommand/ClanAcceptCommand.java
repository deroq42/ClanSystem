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
public class ClanAcceptCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (!currentClan.isLeader(user)) {
                user.sendMessage("Du bist kein Leader dieses Clans");
                return;
            }
            String name = args[0];
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, targetPlayer -> {
                if (targetPlayer == null) {
                    user.sendMessage("Spieler konnte nicht gefunden werden");
                    return;
                }
                ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(targetPlayer);
                Callback.of(userFuture, requestedUser -> {
                    if (requestedUser == null) {
                        user.sendMessage("Spieler konnte nicht gefunden werden");
                        return;
                    }
                    ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
                    Callback.of(requestsFuture, requests -> {
                        if (!requests.contains(requestedUser.getUuid())) {
                            user.sendMessage("Dieser Spieler hat keine Beitrittsanfrage gesendet");
                            return;
                        }
                        if (currentClan.getMembers().size() >= ClanSystem.CLAN_PLAYER_LIMIT) {
                            user.sendMessage("Dieser Clan ist voll");
                            return;
                        }
                        ListenableFuture<Boolean> acceptFuture = clanSystem.getRequestManager().acceptRequest(requestedUser, user, currentClan, requests);
                        Callback.of(acceptFuture, accepted -> {
                            if (accepted) {
                                currentClan.broadcast("§c" + requestedUser.getName() + " §7hat den Clan betreten");
                            }
                        });
                    });
                });
            });
        });
    }
}
