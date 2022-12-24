package de.deroq.clans.api.user;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.AbstractClan;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface AbstractClanUser {


    /**
     * Sends a message to the user.
     *
     * @param translationKey The translation key of the message to send.
     * @param objects        Optional objects to replace the parameters of the message.
     */
    void sendMessage(String translationKey, Object... objects);

    /**
     * Sends a message to the user in the form of a TextComponent. (ONLY FOR BUNGEECORD)
     *
     * @param textComponent The component to send.
     */
    void sendMessage(Object textComponent);

    /**
     * Translates a message by its translation key.
     *
     * @param translationKey The translation key of the message.
     * @param objects        Optional objects to replace the parameters of the message.
     */
    String translate(String translationKey, Object... objects);

    /**
     * Gets the current clan of the user.
     *
     * @return a ListenableFuture with the current clan or null if the user is not in any clan.
     */
    ListenableFuture<AbstractClan> getClan();

    /**
     * Checks if the user is online.
     *
     * @return true if the user is online.
     */
    boolean isOnline();

    /**
     * @return the uuid of the user.
     */
    UUID getUuid();

    /**
     * @return the name of the user.
     */
    String getName();

    /**
     * Sets the clan of the user.
     *
     * @param clan The id of the clan.
     */
    void setClan(UUID clan);

    /**
     * @return the used locale of the player.
     */
    Locale getLocale();

    /**
     * Sets the locale of the user.
     *
     * @param locale The locale to set.
     */
    void setLocale(Locale locale);
}
