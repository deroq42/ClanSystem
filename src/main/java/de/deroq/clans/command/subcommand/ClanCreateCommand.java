package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.user.AbstractClanUser;
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
    public void run(AbstractClanUser user, String[] args) {
        if (args.length != 2) {
            sendHelp(user, 1);
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
                user.sendMessage("clan-name-invalid");
                return;
            }
            if (!ClanSystem.VALID_CLAN_TAGS.matcher(clanTag).matches()) {
                user.sendMessage("clan-tag-invalid");
                return;
            }
            ListenableFuture<Boolean> nameFuture = clanSystem.getClanManager().isNameAvailable(clanName);
            Callback.of(nameFuture, isNameAvailable -> {
                if (!isNameAvailable) {
                    user.sendMessage("clan-name-unavailable");
                    return;
                }
                ListenableFuture<Boolean> tagFuture = clanSystem.getClanManager().isTagAvailable(clanTag);
                Callback.of(tagFuture, isTagAvailable -> {
                    if (!isTagAvailable) {
                        user.sendMessage("clan-tag-unavailable");
                        return;
                    }
                    ListenableFuture<AbstractClan> createClan = clanSystem.getClanManager().createClan(user, clanName, clanTag);
                    Callback.of(createClan, createdClan -> {
                        if (createdClan == null) {
                            user.sendMessage("clan-create-error");
                            return;
                        }
                        user.sendMessage("clan-create", clanName, clanTag);
                    });
                });
            });
        });
    }
}
