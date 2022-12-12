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
 * @since 13.12.2022
 */
@RequiredArgsConstructor
public class ClanDeclineAllCommand extends ClanSubCommand {

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
                requests.stream()
                        .map(uuid -> clanSystem.getUserManager().getUser(uuid))
                        .forEach(userFuture -> {
                            Callback.of(userFuture, requestedUser -> {
                                ListenableFuture<Boolean> declineFuture = clanSystem.getRequestManager().declineRequest(requestedUser, user, currentClan, requests);
                                Callback.of(declineFuture, declined -> {
                                    if (declined) {
                                        requestedUser.sendMessage("Deine Beitrittsanfrage an den Clan §c" + currentClan.getClanName() + " §7wurde abgelehnt");
                                    }
                                });
                            });
                        });
            });
            user.sendMessage("Du hast alle Beitrittsanfragen abgelehnt");
        });
    }
}
