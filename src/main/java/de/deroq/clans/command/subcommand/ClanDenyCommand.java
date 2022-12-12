package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class ClanDenyCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user);
            return;
        }
        String name = args[0].toLowerCase();
        ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(name);
        Callback.of(clanFuture, clan -> {
            if (clan == null) {
                user.sendMessage("Diesen Clan gibt es nicht");
                return;
            }
            ListenableFuture<Set<UUID>> invitesFuture = clanSystem.getInviteManager().getInvites(user.getUuid());
            Callback.of(invitesFuture, invites -> {
                if (!invites.contains(clan.getClanId())) {
                    user.sendMessage("Du hast keine Einladung von diesem Clan erhalten");
                    return;
                }
                ListenableFuture<Boolean> denyFuture = clanSystem.getClanManager().denyInvite(
                        clanSystem,
                        user,
                        clan
                );
                Callback.of(denyFuture, denied -> {
                    if (denied) {
                        user.sendMessage("Du hast die Einladung vom Clan §c" + clan.getClanName() + " §7abgelehnt");
                    }
                });
            });
        });
    }
}
