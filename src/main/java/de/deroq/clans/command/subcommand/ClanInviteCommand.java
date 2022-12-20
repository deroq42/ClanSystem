package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import de.deroq.clans.util.Pair;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
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
    public void run(AbstractUser from, String[] args) {
        if (args.length != 1) {
            sendHelp(from, 1);
            return;
        }
        Callback.of(from.getClan(), currentClan -> {
            if (currentClan == null) {
                from.sendMessage("no-clan");
                return;
            }
            if (currentClan.isDefault(from)) {
                from.sendMessage("clan-cant-invite");
                return;
            }
            String name = args[0];
            if (name.equalsIgnoreCase(from.getName())) {
                from.sendMessage("interact-yourself");
                return;
            }
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, uuid -> {
                if (uuid == null) {
                    from.sendMessage("user-not-found");
                    return;
                }
                ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toInvite -> {
                    if (toInvite == null) {
                        from.sendMessage("user-not-found");
                        return;
                    }
                    if (currentClan.containsUser(toInvite)) {
                        from.sendMessage("user-already-in-clan");
                        return;
                    }
                    ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = clanSystem.getInviteManager().getInvites(uuid);
                    Callback.of(invitesFuture, invites -> {
                        Optional<Pair<UUID, UUID>> optionalInvite = invites.stream()
                                .filter(clanUserPair -> clanUserPair.getKey().equals(currentClan.getClanId()))
                                .findFirst();
                        if (optionalInvite.isPresent()) {
                            from.sendMessage("invites-already-invited");
                            return;

                        }
                        Callback.of(toInvite.getClan(), clan -> {
                            if (clan != null) {
                                from.sendMessage("user-already-in-clan");
                                return;
                            }
                            ListenableFuture<Boolean> inviteFuture = clanSystem.getInviteManager().sendInvite(toInvite, currentClan, from, invites);
                            Callback.of(inviteFuture, invited -> {
                                if (invited) {
                                    from.sendMessage("clan-invite", toInvite.getName());
                                }
                            });
                        });
                    });
                });
            });
        });
    }
}
