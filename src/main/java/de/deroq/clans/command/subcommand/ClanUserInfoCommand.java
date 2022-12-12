package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
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
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user);
            return;
        }
        ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(args[0]);
        Callback.of(uuidFuture, targetPlayer -> {
            if (targetPlayer == null) {
                user.sendMessage("Spieler konnte nicht gefunden werden");
                return;
            }
            ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByPlayer(targetPlayer);
            Callback.of(clanFuture, clan -> {
                if (clan == null) {
                    user.sendMessage("Dieser Spieler ist in keinem Clan");
                    return;
                }
                sendInfo(clanSystem, user, clan, false);
            });
        });
    }
}