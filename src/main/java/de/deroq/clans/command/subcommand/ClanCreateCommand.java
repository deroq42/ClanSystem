package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.Clan;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import lombok.RequiredArgsConstructor;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanCreateCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(ClanUser user, String[] args) {
        if (args.length != 2) {
            sendHelp(user);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan != null) {
                user.sendMessage("Du bist bereits in einem Clan");
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
            ListenableFuture<Boolean> nameFuture = clanSystem.getClanManager().isNameAvailable(clanName);
            Callback.of(nameFuture, isNameAvailable -> {
                if (!isNameAvailable) {
                    user.sendMessage("Es gibt bereits einen Clan mit diesem Namen");
                    return;
                }
                ListenableFuture<Boolean> tagFuture = clanSystem.getClanManager().isTagAvailable(clanTag);
                Callback.of(tagFuture, isTagAvailable -> {
                    if (!isTagAvailable) {
                        user.sendMessage("Es gibt bereits einen Clan mit diesem Tag");
                        return;
                    }
                    ListenableFuture<Clan> createClan = clanSystem.getClanManager().createClan(
                            clanSystem,
                            user,
                            clanName,
                            clanTag
                    );
                    Callback.of(createClan, createdClan -> {
                        if (createdClan == null) {
                            user.sendMessage("Clan konnte nicht erstellt werden");
                            return;
                        }
                        user.sendMessage("Der Clan §c" + clanName + " §7[§c" + clanTag + "§7] wurde erstellt");
                    });
                });
            });
        });
    }
}
