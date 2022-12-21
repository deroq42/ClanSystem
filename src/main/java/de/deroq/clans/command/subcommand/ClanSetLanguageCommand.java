package de.deroq.clans.command.subcommand;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
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
            sendHelp(user, 2);
            return;
        }
        String languageTag = args[0];
        Locale locale = Locale.forLanguageTag(languageTag);
        if (locale == null) {
            user.sendMessage("language-not-supported", clanSystem.getLanguageManager().getSupportedLanguages());
            return;
        }
        ListenableFuture<Boolean> languageFuture = clanSystem.getLanguageManager().isLanguageSupported(locale);
        Callback.of(languageFuture, isSupported -> {
            if (!isSupported) {
                user.sendMessage("language-not-supported", clanSystem.getLanguageManager().getSupportedLanguages());
                return;
            }
            ListenableFuture<Boolean> updateFuture = clanSystem.getUserManager().updateLocale(user, locale);
            Callback.of(updateFuture, updated -> {
                if (updated) {
                    user.sendMessage("language-changed", locale.toLanguageTag());
                }
            });
        });
    }
}
