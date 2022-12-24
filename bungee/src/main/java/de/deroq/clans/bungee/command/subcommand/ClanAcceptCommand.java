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
public class ClanAcceptCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser from, String[] args) {
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
                ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toAccept -> {
                    if (toAccept == null) {
                        from.sendMessage("user-not-found");
                        return;
                    }
                    ListenableFuture<Set<UUID>> requestsFuture = clanSystem.getRequestManager().getRequests(currentClan);
                    Callback.of(requestsFuture, requests -> {
                        if (!requests.contains(toAccept.getUuid())) {
                            from.sendMessage("requests-user-didnt-request");
                            return;
                        }
                        if (currentClan.getMembers().size() >= ClanSystem.CLAN_PLAYER_LIMIT) {
                            from.sendMessage("clan-already-full");
                            return;
                        }
                        ListenableFuture<Boolean> acceptFuture = clanSystem.getRequestManager().acceptRequest(toAccept, currentClan, requests);
                        Callback.of(acceptFuture, accepted -> {
                            if (accepted) {
                                currentClan.broadcast("clan-join", toAccept.getName());
                            }
                        });
                    });
                });
            });
        });
    }
}
