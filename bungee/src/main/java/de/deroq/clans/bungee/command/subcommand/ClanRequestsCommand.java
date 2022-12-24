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
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanRequestsCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            if (!currentClan.isLeader(user)) {
                user.sendMessage("not-leader-of-clan");
                return;
            }
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                if (requests.isEmpty()) {
                    user.sendMessage("requests-no-remaining");
                    return;
                }
                user.sendMessage("requests-list-header");
                for (UUID uuid : requests) {
                    ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                    Callback.of(userFuture, request -> user.sendMessage("requests-list-format", request.getName()));
                }
            });
        });
    }
}
