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
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (currentClan.isDefault(from)) {
                from.sendMessage("Du kannst keine Spieler aus dem Clan werfen");
                return;
            }
            String name = args[0];
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, uuid -> {
                if (uuid == null) {
                    from.sendMessage("Spieler konnte nicht gefunden werden");
                    return;
                }
                ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toKick -> {
                    if (toKick == null) {
                        from.sendMessage("Spieler konnte nicht gefunden werden");
                        return;
                    }
                    if (!currentClan.containsUser(toKick)) {
                        from.sendMessage("Dieser Spieler ist nicht im Clan");
                        return;
                    }
                    if (!currentClan.canKick(toKick, from)) {
                        from.sendMessage("Du kannst diesen Spieler nicht aus dem Clan werfen");
                        return;
                    }
                    ListenableFuture<Boolean> kickFuture = clanSystem.getClanManager().leaveClan(toKick, currentClan);
                    Callback.of(kickFuture, kicked -> {
                        if (kicked) {
                            toKick.sendMessage("Du wurdest von §c" + from.getName() + " §7aus dem Clan geworfen");
                            currentClan.broadcast("§c" + from.getName() + " §7hat §c" + toKick.getName() + " §7aus dem Clan geworfen");
                        }
                    });
                });
            });
        });
    }
}
