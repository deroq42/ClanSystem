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
public class ClanDeclineCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser from, String[] args) {
        if (args.length != 1) {
            sendHelp(from);
            return;
        }
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (!currentClan.isLeader(from)) {
                from.sendMessage("Du bist kein Leader dieses Clans");
                return;
            }
            String name = args[0];
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, uuid -> {
                if (uuid == null) {
                    from.sendMessage("Spieler konnte nicht gefunden werden");
                    return;
                }
                ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toDecline -> {
                    if (toDecline == null) {
                        from.sendMessage("Spieler konnte nicht gefunden werden");
                        return;
                    }
                    ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
                    Callback.of(requestsFuture, requests -> {
                        if (!requests.contains(toDecline.getUuid())) {
                            from.sendMessage("Dieser Spieler hat keine Beitrittsanfrage gesendet");
                            return;
                        }
                        ListenableFuture<Boolean> declineFuture = clanSystem.getRequestManager().declineRequest(toDecline, currentClan, requests);
                        Callback.of(declineFuture, declined -> {
                            if (declined) {
                                toDecline.sendMessage("Deine Beitrittsanfrage an den Clan §c" + currentClan.getClanName() + " §7wurde abgelehnt");
                            }
                        });
                    });
                });
            });
        });
    }
}
