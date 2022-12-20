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
            sendHelp(from, 3);
            return;
        }
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("no-clan");
                return;
            }
            if (!currentClan.isLeader(from)) {
                from.sendMessage("not-leader-of-clan");
                return;
            }
            String name = args[0];
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, uuid -> {
                if (uuid == null) {
                    from.sendMessage("user-not-found");
                    return;
                }
                ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toDecline -> {
                    if (toDecline == null) {
                        from.sendMessage("user-not-found");
                        return;
                    }
                    ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
                    Callback.of(requestsFuture, requests -> {
                        if (!requests.contains(toDecline.getUuid())) {
                            from.sendMessage("requests-user-didnt-request");
                            return;
                        }
                        ListenableFuture<Boolean> declineFuture = clanSystem.getRequestManager().declineRequest(toDecline, currentClan, requests);
                        Callback.of(declineFuture, declined -> {
                            if (declined) {
                                toDecline.sendMessage("requests-request-declined", toDecline.getName());
                            }
                        });
                    });
                });
            });
        });
    }
}
