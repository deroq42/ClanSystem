package de.deroq.clans.command.subcommand;

import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class ClanInfoCommand extends ClanSubCommand {

    @Override
    public void run(ClanUser user, String[] args) {
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("Du bist in keinem Clan");
                return;
            }
            sendInfo(user, currentClan, true);
        });
    }
}
