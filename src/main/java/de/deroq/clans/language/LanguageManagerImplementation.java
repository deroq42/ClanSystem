package de.deroq.clans.language;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.language.exception.LocaleLoadException;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 19.12.2022
 */
public class LanguageManagerImplementation implements LanguageManager {

    @Getter
    private final File localesFolder;

    private final Logger logger;

    @Getter
    private final Set<Locale> loadedLocales = new HashSet<>();

    @Getter
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();

    public LanguageManagerImplementation(File localesFolder, Logger logger) {
        this.localesFolder = localesFolder;
        this.logger = logger;
    }

    @Override
    public synchronized ListenableFuture<Boolean> loadLocales(boolean log) throws LocaleLoadException {
        if (!localesFolder.exists()) {
            localesFolder.mkdirs();
        }
        File[] files = localesFolder.listFiles();
        if (files == null) {
            return Futures.immediateFuture(false);
        }
        boolean translated = false;
        for (File file : files) {
            if (file.getName().endsWith(".properties")) {
                String languageTag = file.getName().substring(0, file.getName().indexOf("."));
                Locale locale = Locale.forLanguageTag(languageTag);
                if (locale == null) {
                    throw new LocaleLoadException("Error while loading Locale of language tag " + languageTag + ": Language tag could not be found.");
                }
                loadedLocales.add(locale);
                if (log) {
                    logger.info("Locale " + languageTag + " has been loaded.");
                }
                translated = translateLocale(locale, log);
            }
        }
        return Futures.immediateFuture(translated);
    }

    @Override
    public boolean translateLocale(Locale locale, boolean log) {
        try (InputStream inputStream = Files.newInputStream(new File(localesFolder.getPath(), locale.toLanguageTag() + ".properties").toPath())) {
            Properties properties = new Properties();
            properties.load(inputStream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Map<String, String> map = translations.computeIfAbsent(locale, o -> new HashMap<>());
                String translationKey = (String) entry.getKey();
                String translation = (String) entry.getValue();
                translation = translation
                        .replace("%prefix%", properties.getProperty("prefix"))
                        .replace('&', '§');
                map.put(translationKey, translation);
            }
            if (log) {
                logger.info("Translations of Locale " + locale.toLanguageTag() + " have been loaded.");
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String translate(Locale locale, String translationKey, Object... objects) {
        if (!translations.containsKey(locale)) {
            return "N/A";
        }
        if (!translations.get(locale).containsKey(translationKey)) {
            return "N/A";
        }
        String message = translations.get(locale).get(translationKey);
        return MessageFormat.format(message, objects);
    }

    @Override
    public ListenableFuture<Boolean> startRefreshing(ClanSystem clanSystem) {
        ProxyServer.getInstance().getScheduler().schedule(clanSystem, () -> ProxyServer.getInstance().getScheduler().runAsync(clanSystem, this::refresh), 5, TimeUnit.MINUTES);
        return Futures.immediateFuture(true);
    }

    @Override
    public synchronized ListenableFuture<Boolean> refresh() {
        clearUp();
        ListenableFuture<Boolean> future;
        try {
            future = loadLocales(false);
        } catch (LocaleLoadException e) {
            throw new RuntimeException(e);
        }
        logger.info("Locales have been refreshed.");
        return future;
    }

    @Override
    public synchronized void clearUp() {
        loadedLocales.clear();
        translations.clear();
    }

    @Override
    public ListenableFuture<Boolean> isLanguageSupported(Locale locale) {
        return Futures.immediateFuture(loadedLocales.contains(locale));
    }

    @Override
    public Set<String> getSupportedLanguages() {
        return loadedLocales.stream()
                .map(Locale::toLanguageTag)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getTranslations(Locale locale) {
        return translations.get(locale);
    }
}
