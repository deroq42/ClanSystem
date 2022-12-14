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
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanJoinCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 1);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null) {
                user.sendMessage("already-in-clan");
                return;
            }
            String name = args[0].toLowerCase();
            ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanByName(name);
            Callback.of(clanFuture, toJoin -> {
                if (toJoin == null) {
                    user.sendMessage("clan-not-found");
                    return;
                }
                ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = clanSystem.getInviteManager().getInvites(user);
                Callback.of(invitesFuture, invites -> {
                    Optional<Pair<UUID, UUID>> optionalInvite = invites.stream()
                            .filter(clanUserPair -> clanUserPair.getKey().equals(toJoin.getClanId()))
                            .findFirst();
                    if (!optionalInvite.isPresent()) {
                        user.sendMessage("invites-clan-didnt-invite");
                        return;
                    }
                    if (toJoin.getMembers().size() >= ClanSystem.CLAN_PLAYER_LIMIT) {
                        user.sendMessage("clan-already-full");
                        return;
                    }
                    ListenableFuture<Boolean> joinFuture = clanSystem.getClanManager().joinClan(user, toJoin);
                    Callback.of(joinFuture, joined -> {
                        if (joined) {
                            toJoin.broadcast("clan-join", user.getName());
                        }
                    });
                });
            });
        });
    }
}
