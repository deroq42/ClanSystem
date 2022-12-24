package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanNameInfoCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
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
