package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
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
    public void run(AbstractClanUser from, String[] args) {
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("no-clan");
                return;
            }
            if (!currentClan.isLeader(from)) {
                from.sendMessage("not-leader-of-clan");
                return;
            }
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                if (requests.isEmpty()) {
                    from.sendMessage("requests-no-remaining");
                    return;
                }
                requests.stream()
                        .map(uuid -> clanSystem.getUserManager().getUser(uuid))
                        .forEach(userFuture -> {
                            Callback.of(userFuture, toDecline -> {
                                ListenableFuture<Boolean> declineFuture = clanSystem.getRequestManager().declineRequest(toDecline, currentClan, requests);
                                Callback.of(declineFuture, declined -> {
                                    if (declined) {
                                        toDecline.sendMessage("requests-declined", toDecline.getName());
                                    }
                                });
                            });
                        });
            });
            from.sendMessage("requests-declined-all");
        });
    }
}
