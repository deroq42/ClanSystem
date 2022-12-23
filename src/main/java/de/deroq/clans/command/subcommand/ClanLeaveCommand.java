package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.AbstractClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class ClanLeaveCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            if (currentClan.isLeader(user)) {
                if (currentClan.getInfo().getUsersWithGroup(Clan.Group.LEADER).size() == 1) {
                    user.sendMessage("clan-sole-leader");
                    return;
                }
            }
            if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
                user.sendMessage("clan-leave-confirm");
                return;
            }
            ListenableFuture<Boolean> leaveFuture = clanSystem.getClanManager().leaveClan(user, currentClan);
            Callback.of(leaveFuture, left -> {
                if (left) {
                    user.sendMessage("clan-you-left");
                    currentClan.broadcast("clan-leave", user.getName());
                }
            });
        });
    }
}
