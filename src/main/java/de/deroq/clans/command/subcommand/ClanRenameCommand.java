package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
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
    public void run(AbstractUser user, String[] args) {
        if (args.length != 2) {
            sendHelp(user, 1);
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            if (currentClan.isLeader(user)) {
                user.sendMessage("not-leader-of-clan");
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
            Callback.of(nameFuture, nameAvailable -> {
                if (!clanName.equalsIgnoreCase(currentClan.getClanName())) {
                    if (!nameAvailable) {
                        user.sendMessage("clan-name-unavailable");
                        return;
                    }
                }
                ListenableFuture<Boolean> tagFuture = clanSystem.getClanManager().isTagAvailable(clanTag);
                Callback.of(tagFuture, tagAvailable -> {
                    if (!clanTag.equalsIgnoreCase(currentClan.getClanTag())) {
                        if (!tagAvailable) {
                            user.sendMessage("clan-tag-unavailable");
                            return;
                        }
                    }
                    if (clanName.equals(currentClan.getClanName())
                            && clanTag.equals(currentClan.getClanTag())) {
                        user.sendMessage("clan-rename-error");
                        return;
                    }
                    ListenableFuture<Boolean> future = clanSystem.getClanManager().renameClan(currentClan, clanName, clanTag);
                    Callback.of(future, renamed -> {
                        if (renamed) {
                            currentClan.broadcast("clan-rename", clanName, clanTag);
                        }
                    });
                });
            });
        });
    }
}
