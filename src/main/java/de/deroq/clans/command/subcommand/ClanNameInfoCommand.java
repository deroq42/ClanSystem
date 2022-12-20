package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanNameInfoCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 2);
            return;
        }
        ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(args[0]);
        Callback.of(clanFuture, clan -> {
            if (clan == null) {
                user.sendMessage("no-clan");
                return;
            }
            sendInfo(clanSystem, user, clan, false);
        });
    }
}
