package de.deroq.clans.language;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;

/**
 * @author Miles
 * @since 19.12.2022
 */
public interface LanguageManager {

    Set<Locale> findLocales();

    ListenableFuture<Boolean> loadLocales(boolean log);

    boolean translateLocale(Locale locale, boolean log);

    String translate(Locale locale, String translationKey, Object... objects);

    ListenableFuture<Boolean> startRefreshing();

    ListenableFuture<Boolean> refresh();

    ListenableFuture<Boolean> clearUp();

    ListenableFuture<Boolean> isLanguageSupported(Locale locale);

    Set<String> getSupportedLanguages();

    Set<Locale> getLoadedLocales();
}
