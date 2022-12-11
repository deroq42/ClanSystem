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
public class ClanJoinCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(ClanUser user, String[] args) {
        if (args.length != 1) {
            // Send help.
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null) {
                user.sendMessage("Du bist bereits in einem Clan");
                return;
            }
            String name = args[0];
            ListenableFuture<Clan> clanFuture = clanSystem.getClanManager().getClanByName(name);
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
                    ListenableFuture<Boolean> joinFuture = clanSystem.getClanManager().joinClan(user, clan);
                    Callback.of(joinFuture, joined -> {
                        if (joined) {
                            clan.broadcast("§c" + user.getName() + " §7hat den Clan betreten");
                        }
                    });
                });
            });
        });
    }
}
