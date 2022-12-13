package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.AbstractClan;
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
public class ClanRequestCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null) {
                user.sendMessage("Du bist bereits in einem Clan");
                return;
            }
            String name = args[0];
            ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(name);
            Callback.of(clanFuture, toRequest -> {
                if (toRequest == null) {
                    user.sendMessage("Diesen Clan gibt es nicht");
                    return;
                }
                ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(toRequest);
                Callback.of(requestsFuture, requests -> {
                    if (requests.contains(user.getUuid())) {
                        user.sendMessage("Du hast diesem Clan bereits eine Beitrittsanfrage gesendet");
                        return;
                    }
                    ListenableFuture<Boolean> requestFuture = clanSystem.getRequestManager().sendRequest(toRequest, user, requests);
                    Callback.of(requestFuture, requested -> {
                        if (requested) {
                            user.sendMessage("Du hast eine Beitrittsanfrage an den Clan §c" + toRequest.getClanName() + " §7gesendet");
                        }
                    });
                });
            });
        });
    }
}
