package de.deroq.clans.command.subcommand;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * @author Miles
 * @since 20.12.2022
 */
@RequiredArgsConstructor
public class ClanSetLanguageCommand extends ClanSubCommand {

    private final ClanSystem clanSystem;

    @Override
    public void run(AbstractUser user, String[] args) {
        if (args.length != 1) {
            sendHelp(user, 1);
            return;
        }
        String languageTag = args[0];
        Locale locale = Locale.forLanguageTag(languageTag);
        if (locale == null) {
            return;
        }
        if (clanSystem.getLanguageManager().isLanguageSupported(locale)) {
            clanSystem.getUserManager().updateLocale(user, locale);
        }
    }
}
