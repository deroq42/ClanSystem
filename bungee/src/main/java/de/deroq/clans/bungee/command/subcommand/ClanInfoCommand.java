package de.deroq.clans.bungee.command.subcommand;

import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bungee.command.ClanSubCommand;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class ClanInfoCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractClanUser user, String[] args) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            sendInfo(clanSystem, user, currentClan, true);
        });
    }
}
