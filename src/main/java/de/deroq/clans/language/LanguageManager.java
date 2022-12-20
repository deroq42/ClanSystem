package de.deroq.clans.language;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.language.exception.LocaleLoadException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface LanguageManager {

    void loadLocales(boolean log) throws LocaleLoadException;

    void translateLocale(Locale locale, boolean log);

    String translate(Locale locale, String translationKey, Object... objects);

    void startRefreshing(ClanSystem clanSystem);

    void refresh();

    void clearUp();

    boolean isLanguageSupported(Locale locale);

    Set<Locale> getLoadedLocales();

    Map<Locale, Map<String, String>> getTranslations();

    Map<String, String> getTranslations(Locale locale);
}
