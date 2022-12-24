package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Pair;
import de.deroq.clans.bungee.command.ClanSubCommand;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
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
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 1);
            return;
        }
        String name = args[0].toLowerCase();
        ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(name);
        Callback.of(clanFuture, toDeny -> {
            if (toDeny == null) {
                user.sendMessage("clan-not-found");
                return;
            }
            ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = clanSystem.getInviteManager().getInvites(user);
            Callback.of(invitesFuture, invites -> {
                Optional<Pair<UUID, UUID>> optionalInvite = invites.stream()
                        .filter(clanUserPair -> clanUserPair.getKey().equals(toDeny.getClanId()))
                        .findFirst();
                if (!optionalInvite.isPresent()) {
                    user.sendMessage("invites-clan-didnt-invite");
                    return;
                }
                ListenableFuture<Boolean> denyFuture = clanSystem.getInviteManager().denyInvite(user, toDeny, invites);
                Callback.of(denyFuture, denied -> {
                    if (denied) {
                        user.sendMessage("invites-denied", toDeny.getClanName());
                    }
                });
            });
        });
    }
}
