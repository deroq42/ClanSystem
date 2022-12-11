package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanInviteCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(ClanUser inviter, String[] args) {
        if (args.length != 1) {
            // Send help.
            return;
        }
        Callback.of(inviter.getClan(), currentClan -> {
            if (currentClan == null) {
                inviter.sendMessage("Du bist in keinem Clan");
                return;
            }
            if (currentClan.isDefault(inviter.getUuid())) {
                inviter.sendMessage("Du kannst keine Spieler in den Clan einladen");
                return;
            }
            String name = args[0];
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, targetPlayer -> {
                if (targetPlayer == null) {
                    inviter.sendMessage("Spieler konnte nicht gefunden werden");
                    return;
                }
                ListenableFuture<ClanUser> userFuture = clanSystem.getUserManager().getUser(targetPlayer);
                Callback.of(userFuture, invited -> {
                    if (invited == null) {
                        inviter.sendMessage("Spieler konnte nicht gefunden werden");
                        return;
                    }
                    if (currentClan.containsUser(targetPlayer)) {
                        inviter.sendMessage("Dieser Spieler ist bereits im Clan");
                        return;
                    }
                    ListenableFuture<Set<UUID>> invitesFuture = clanSystem.getInviteManager().getInvites(targetPlayer);
                    Callback.of(invitesFuture, invites -> {
                        if (invites.contains(currentClan.getClanId())) {
                            inviter.sendMessage("Dieser Spieler wurde bereits eingeladen");
                            return;
                        }
                        Callback.of(invited.getClan(), clan -> {
                            if (clan != null) {
                                inviter.sendMessage("Dieser Spieler ist bereits in einem Clan");
                                return;
                            }
                            clanSystem.getInviteManager().sendInvite(
                                    invited,
                                    currentClan,
                                    inviter,
                                    invites
                            );
                        });
                    });
                });
            });
        });
    }
}
