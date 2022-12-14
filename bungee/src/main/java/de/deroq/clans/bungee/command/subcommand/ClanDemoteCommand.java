package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
import de.deroq.clans.bungee.Clan;
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
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 2);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            if (!currentClan.isLeader(user)) {
                user.sendMessage("not-leader-of-clan");
                return;
            }
            String name = args[0];
            if (name.equalsIgnoreCase(user.getName())) {
                user.sendMessage("interact-yourself");
                return;
            }
            ListenableFuture<UUID> uuidFuture = clanSystem.getUserManager().getUUID(name);
            Callback.of(uuidFuture, uuid -> {
                if (uuid == null) {
                    user.sendMessage("user-not-found");
                    return;
                }
                ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(uuid);
                Callback.of(userFuture, toDemote -> {
                    if (toDemote == null) {
                        user.sendMessage("user-not-found");
                        return;
                    }
                    if (!currentClan.containsUser(toDemote)) {
                        user.sendMessage("user-not-in-clan");
                        return;
                    }
                    ListenableFuture<Clan.Group> groupFuture = clanSystem.getClanManager().demoteUser(toDemote, currentClan);
                    Callback.of(groupFuture, group -> {
                        if (group == null) {
                            user.sendMessage("user-already-member");
                        } else {
                            currentClan.broadcast(member -> member.sendMessage("clan-group-change", toDemote.getName(), user.translate(group.getTranslationKey())));
                        }
                    });
                });
            });
        });
    }
}
