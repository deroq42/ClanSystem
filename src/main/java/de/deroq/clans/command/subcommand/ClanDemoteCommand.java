package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class ClanDemoteCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(ClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (!currentClan.isLeader(user)) {
                user.sendMessage("Du bist kein Leader dieses Clans");
                return;
            }
            String name = args[0];
            if (name.equalsIgnoreCase(user.getName())) {
                user.sendMessage("Du kannst nicht mit dir selber interagieren");
                return;
            }
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, targetPlayer -> {
                if (targetPlayer == null) {
                    user.sendMessage("Spieler konnte nicht gefunden werden");
                    return;
                }
                ListenableFuture<ClanUser> userFuture = clanSystem.getUserManager().getUser(targetPlayer);
                Callback.of(userFuture, demotedUser -> {
                    if (demotedUser == null) {
                        user.sendMessage("Spieler konnte nicht gefunden werden");
                        return;
                    }
                    if (!currentClan.containsUser(demotedUser)) {
                        user.sendMessage("Dieser Spieler ist nicht im Clan");
                        return;
                    }
                    ListenableFuture<Clan.Group> groupFuture =  clanSystem.getClanManager().demoteUser(
                            demotedUser,
                            currentClan
                    );
                    Callback.of(groupFuture, group -> {
                        if (group == null) {
                            user.sendMessage("Dieser Spieler ist bereits Mitglied");
                        } else {
                            currentClan.broadcast("§c" + demotedUser.getName() + " §7ist nun §c" + group.getText());
                        }
                    });
                });
            });
        });
    }
}