package de.deroq.clans.bungee.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Pair;
import de.deroq.clans.bungee.command.ClanSubCommand;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 20.12.2022
 */
@RequiredArgsConstructor
public class ClanInvitesCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        ListenableFuture<Set<Pair<UUID, UUID>>> invitesFuture = clanSystem.getInviteManager().getInvites(user);
        Callback.of(invitesFuture, invites -> {
            if (invites.isEmpty()) {
                user.sendMessage("invites-no-remaining");
                return;
            }
            user.sendMessage("invites-list-header");
            for (Pair<UUID, UUID> pair : invites) {
                ListenableFuture<AbstractClan> clanFuture = clanSystem.getClanManager().getClanById(pair.getKey());
                Callback.of(clanFuture, invite -> user.sendMessage("invites-list-format", invite.getClanName()));
            }
        });
    }
}
