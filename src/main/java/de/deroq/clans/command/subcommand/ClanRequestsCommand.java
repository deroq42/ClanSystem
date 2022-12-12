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
public class ClanRequestsCommand extends ClanSubCommand {

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
            user.sendMessage("Beitrittsanfragen");
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                for (UUID uuid : requests) {
                    ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                    Callback.of(userFuture, request -> user.sendMessage(request.getName()));
                }
            });
        });
    }
}
