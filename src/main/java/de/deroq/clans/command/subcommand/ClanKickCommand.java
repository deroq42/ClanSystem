package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Miles
 * @since 13.12.2022
 */
@RequiredArgsConstructor
public class ClanKickCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser from, String[] args) {
        if (args.length != 1) {
            sendHelp(from, 2);
            return;
        }
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("no-clan");
                return;
            }
            if (currentClan.isDefault(from)) {
                from.sendMessage("clan-cant-kick");
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
                Callback.of(userFuture, toKick -> {
                    if (toKick == null) {
                        from.sendMessage("user-not-found");
                        return;
                    }
                    if (!currentClan.containsUser(toKick)) {
                        from.sendMessage("user-not-in-clan");
                        return;
                    }
                    if (!currentClan.canKick(toKick, from)) {
                        from.sendMessage("clan-cant-kick-user");
                        return;
                    }
                    ListenableFuture<Boolean> kickFuture = clanSystem.getClanManager().leaveClan(toKick, currentClan);
                    Callback.of(kickFuture, kicked -> {
                        if (kicked) {
                            toKick.sendMessage("clan-got-kicked", from.getName());
                            currentClan.broadcast("clan-kick", from.getName(), toKick.getName());
                        }
                    });
                });
            });
        });
    }
}
