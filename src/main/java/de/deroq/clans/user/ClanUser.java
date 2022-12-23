package de.deroq.clans.user;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.AbstractClan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
@AllArgsConstructor
public class ClanUser implements AbstractClanUser {

    private final ClanSystem clanSystem;

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Setter
    private UUID clan;

    @Getter
    @Setter
    private Locale locale;

    @Override
    public void sendMessage(String translationKey, Object... objects) {
        if (getPlayer() != null) {
            String translation = clanSystem.getLanguageManager().translate(locale, translationKey, objects);
            getPlayer().sendMessage(TextComponent.fromLegacyText(translation));
        }
    }

    @Override
    public void sendMessage(TextComponent textComponent) {
        if (getPlayer() != null) {
            getPlayer().sendMessage(textComponent);
        }
    }

    @Override
    public String translate(String translationKey, Object... objects) {
        return clanSystem.getLanguageManager().translate(locale, translationKey, objects);
    }

    @Override
    public ListenableFuture<AbstractClan> getClan() {
        if (clan == null) {
            return Futures.immediateFuture(null);
        }
        return clanSystem.getDataRepository().getClanById(clan);
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(uuid);
    }
}
