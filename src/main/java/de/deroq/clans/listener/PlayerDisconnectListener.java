package de.deroq.clans.listener;

import de.deroq.clans.ClanSystem;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Miles
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public class PlayerDisconnectListener implements Listener {

    private final ClanSystem clanSystem;

    @EventHandler
    public void onServerDisconnect(PlayerDisconnectEvent event) {
        clanSystem.getUserManager().invalidateOnlineUser(event.getPlayer().getUniqueId());
    }
}
