package de.deroq.clans.user;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.model.Clan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
@AllArgsConstructor
public class ClanUser {

    private final ClanSystem clanSystem;

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Setter
    private UUID clan;

    public void sendMessage(String message) {
        if (getPlayer() != null) {
            getPlayer().sendMessage(TextComponent.fromLegacyText(ClanSystem.PREFIX + message));
        }
    }

    public ListenableFuture<Clan> getClan() {
        if (clan == null) {
            return Futures.immediateFuture(null);
        }
        return clanSystem.getClanDataRepository().getClanById(clan);
    }

    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(uuid);
    }
}
