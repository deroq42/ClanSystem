package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanRenameCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(ClanUser user, String[] args) {
        if (args.length != 2) {
            // Send help.
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("Du bist in keinem Clan");
                return;
            }
            String clanName = args[0];
            String clanTag = args[1];
            if (!ClanSystem.VALID_CLAN_NAMES.matcher(clanName).matches()) {
                user.sendMessage("Dein Name darf keine Sonderzeichen haben und muss zwischen 3 und 16 Zeichen lang sein");
                return;
            }
            if (!ClanSystem.VALID_CLAN_TAGS.matcher(clanTag).matches()) {
                user.sendMessage("Dein Tag darf keine Sonderzeichen haben und muss zwischen 2 und 5 Zeichen lang sein");
                return;
            }
            Callback.of(clanSystem.getClanManager().isNameAvailable(clanName), isNameAvailable -> {
                if (!clanName.equalsIgnoreCase(currentClan.getClanName())) {
                    if (!isNameAvailable) {
                        user.sendMessage("Es gibt bereits einen Clan mit diesem Namen");
                        return;
                    }
                }
                Callback.of(clanSystem.getClanManager().isTagAvailable(clanTag), isTagAvailable -> {
                    if (!clanTag.equalsIgnoreCase(currentClan.getClanTag())) {
                        if (!isTagAvailable) {
                            user.sendMessage("Es gibt bereits einen Clan mit diesem Tag");
                            return;
                        }
                    }
                    if (clanName.equals(currentClan.getClanName()) && clanTag.equals(currentClan.getClanTag())) {
                        user.sendMessage("Du musst den Namen oder den Tag ändern");
                        return;
                    }
                    ListenableFuture<Boolean> future = clanSystem.getClanManager().renameClan(currentClan, clanName, clanTag);
                    Callback.of(future, renamed -> {
                        if (renamed) {
                            currentClan.broadcast("Der Clan heißt nun §c" + clanName + " §7[§c" + clanTag + "§7]");
                        }
                    });
                });
            });
        });
    }
}
