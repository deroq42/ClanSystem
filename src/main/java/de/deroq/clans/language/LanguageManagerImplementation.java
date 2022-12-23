package de.deroq.clans.language;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Miles
 * @since 19.12.2022
 */
public class LanguageManagerImplementation implements LanguageManager {

    private final ClanSystem clanSystem;
    private final File localesFolder;
    private final Set<Locale> loadedLocales = new HashSet<>();
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();

    public LanguageManagerImplementation(ClanSystem clanSystem, File localesFolder) {
        this.clanSystem = clanSystem;
        this.localesFolder = localesFolder;
    }

    @Override
    public Set<Locale> findLocales() {
        return Arrays.stream(Objects.requireNonNull(localesFolder.listFiles()))
                .filter(file -> file.getName().endsWith(".properties"))
                .map(this::getNameWithoutExtension)
                .map(Locale::forLanguageTag)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized ListenableFuture<Boolean> loadLocales(boolean log) {
        if (!localesFolder.exists()) {
            localesFolder.mkdirs();
        }
        File[] files = localesFolder.listFiles();
        if (files == null) {
            return Futures.immediateFuture(false);
        }
        AtomicBoolean translated = new AtomicBoolean(false);
        findLocales().forEach(locale -> {
            loadedLocales.add(locale);
            clanSystem.getLogger().info("Locale " + locale.toLanguageTag() + " has been loaded.");
            translated.set(translateLocale(locale, log));
        });
        return Futures.immediateFuture(translated.get());
    }

    @Override
    public boolean translateLocale(Locale locale, boolean log) {
        try (InputStream inputStream = Files.newInputStream(new File(localesFolder.getPath(), locale.toLanguageTag() + ".properties").toPath())) {
            Properties properties = new Properties();
            properties.load(inputStream);
            translateMessages(properties, locale);
            if (log) {
                clanSystem.getLogger().info("Translations of Locale " + locale.toLanguageTag() + " have been loaded.");
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
    public ListenableFuture<Boolean> startRefreshing() {
        ProxyServer.getInstance().getScheduler().schedule(clanSystem, () -> ProxyServer.getInstance().getScheduler().runAsync(clanSystem, this::refresh), 5, TimeUnit.MINUTES);
        return Futures.immediateFuture(true);
    }

    @Override
    public synchronized ListenableFuture<Boolean> refresh() {
        clearUp();
        ListenableFuture<Boolean> future = loadLocales(false);
        clanSystem.getLogger().info("Locales have been refreshed.");
        return future;
    }

    @Override
    public synchronized ListenableFuture<Boolean> clearUp() {
        loadedLocales.clear();
        translations.clear();
        return Futures.immediateFuture(loadedLocales.size() == 0 && translations.size() == 0);
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
    public Set<Locale> getLoadedLocales() {
        return loadedLocales;
    }

    private void translateMessages(Properties properties, Locale locale) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Map<String, String> map = translations.computeIfAbsent(locale, o -> new HashMap<>());
            String translationKey = (String) entry.getKey();
            String translation = (String) entry.getValue();
            translation = translation
                    .replace("%prefix%", properties.getProperty("prefix"))
                    .replace('&', '§');
            map.put(translationKey, translation);
        }
    }

    private String getNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().indexOf("."));
    }
}
