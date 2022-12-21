package de.deroq.clans.language;

import com.google.common.util.concurrent.ListenableFuture;
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

    ListenableFuture<Boolean> loadLocales(boolean log) throws LocaleLoadException;

    boolean translateLocale(Locale locale, boolean log);

    String translate(Locale locale, String translationKey, Object... objects);

    ListenableFuture<Boolean> startRefreshing(ClanSystem clanSystem);

    ListenableFuture<Boolean> refresh();

    void clearUp();

    ListenableFuture<Boolean> isLanguageSupported(Locale locale);

    Set<String> getSupportedLanguages();

    Set<Locale> getLoadedLocales();

    Map<Locale, Map<String, String>> getTranslations();

    Map<String, String> getTranslations(Locale locale);
}
