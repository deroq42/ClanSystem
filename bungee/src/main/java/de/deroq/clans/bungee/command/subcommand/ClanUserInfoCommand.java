package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanUserInfoCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 2);
            return;
        }
        ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(args[0]);
        Callback.of(uuidFuture, targetPlayer -> {
            if (targetPlayer == null) {
                user.sendMessage("user-not-found");
                return;
            }
            ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByPlayer(targetPlayer);
            Callback.of(clanFuture, clan -> {
                if (clan == null) {
                    user.sendMessage("user-not-in-clan");
                    return;
                }
                sendInfo(clanSystem, user, clan, false);
            });
        });
    }
}
