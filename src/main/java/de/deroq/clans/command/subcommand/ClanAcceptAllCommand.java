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
public class ClanAcceptAllCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser from, String[] args) {
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("user-no-clan");
                return;
            }
            if (!currentClan.isLeader(from)) {
                from.sendMessage("user-not-leader-of-clan");
                return;
            }
            ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
            Callback.of(requestsFuture, requests -> {
                if (requests.isEmpty()) {
                    from.sendMessage("requests-no-remaining");
                    return;
                }
                if (currentClan.getMembers().size() + requests.size() >= ClanSystem.CLAN_PLAYER_LIMIT) {
                    from.sendMessage("requests-clan-limit-reached");
                    return;
                }
                requests.stream()
                        .map(uuid -> clanSystem.getUserManager().getUser(uuid))
                        .forEach(userFuture -> {
                            Callback.of(userFuture, toAccept -> {
                                Callback.of(toAccept.getClan(), clan -> {
                                    if (clan == null) {
                                        ListenableFuture<Boolean> acceptFuture = clanSystem.getRequestManager().acceptRequest(toAccept, currentClan, requests);
                                        Callback.of(acceptFuture, accepted -> {
                                            if (accepted) {
                                                currentClan.broadcast("clan-join", toAccept.getName());
                                            }
                                        });
                                    } else {
                                        from.sendMessage("user-already-in-clan");
                                    }
                                });
                            });
                        });
            });
            from.sendMessage("requests-accepted-all");
        });
    }
}
