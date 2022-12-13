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
    public void run(AbstractUser from, String[] args) {
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (!currentClan.isLeader(from)) {
                from.sendMessage("Du bist kein Leader dieses Clans");
                return;
            }
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                if (requests.isEmpty()) {
                    from.sendMessage("Es sind keine Beitrittsanfragen offen");
                    return;
                }
                requests.stream()
                        .map(uuid -> clanSystem.getUserManager().getUser(uuid))
                        .forEach(userFuture -> {
                            Callback.of(userFuture, toDecline -> {
                                ListenableFuture<Boolean> declineFuture = clanSystem.getRequestManager().declineRequest(toDecline, currentClan, requests);
                                Callback.of(declineFuture, declined -> {
                                    if (declined) {
                                        toDecline.sendMessage("Deine Beitrittsanfrage an den Clan §c" + currentClan.getClanName() + " §7wurde abgelehnt");
                                    }
                                });
                            });
                        });
            });
            from.sendMessage("Du hast alle Beitrittsanfragen abgelehnt");
        });
    }
}
