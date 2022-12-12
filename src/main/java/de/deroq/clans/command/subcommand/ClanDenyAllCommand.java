package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.Pair;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ClanDenyAllCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = clanSystem.getInviteManager().getInvites(user.getUuid());
        Callback.of(invitesFuture, invites -> {
            if (invites.isEmpty()) {
                user.sendMessage("Du hast keine offenen Einladungen");
                return;
            }
            ListenableFuture<Boolean> denyFuture = clanSystem.getInviteManager().denyAllInvites(user, invites);
            Callback.of(denyFuture, denied -> {
                if (denied) {
                    user.sendMessage("Du hast alle offenen Einladungen abgelehnt");
                }
            });
        });
    }
}
