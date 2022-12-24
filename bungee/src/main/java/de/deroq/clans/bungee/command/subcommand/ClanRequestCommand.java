package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
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
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 3);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null) {
                user.sendMessage("no-clan");
                return;
            }
            String name = args[0];
            ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(name);
            Callback.of(clanFuture, toRequest -> {
                if (toRequest == null) {
                    user.sendMessage("clan-not-found");
                    return;
                }
                ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(toRequest);
                Callback.of(requestsFuture, requests -> {
                    if (requests.contains(user.getUuid())) {
                        user.sendMessage("requests-already-requested");
                        return;
                    }
                    ListenableFuture<Boolean> requestFuture = clanSystem.getRequestManager().sendRequest(toRequest, user, requests);
                    Callback.of(requestFuture, requested -> {
                        if (requested) {
                            user.sendMessage("requests-sent", toRequest.getClanName());
                        }
                    });
                });
            });
        });
    }
}
