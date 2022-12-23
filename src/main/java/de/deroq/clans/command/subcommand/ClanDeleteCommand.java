package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanDeleteCommand extends ClanSubCommand {

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
            if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
                user.sendMessage("clan-delete-confirm");
                return;
            }
            ListenableFuture<Boolean> future = clanSystem.getClanManager().deleteClan(currentClan);
            Callback.of(future, deleted -> {
                if (deleted) {
                    currentClan.broadcast("clan-deleted");
                }
            });
        });
    }
}
