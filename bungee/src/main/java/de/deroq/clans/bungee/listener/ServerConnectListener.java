package de.deroq.clans.bungee.listener;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Miles
 * @since 12.12.2022
 */
@RequiredArgsConstructor
public class ServerConnectListener implements Listener {

    private final ClanSystem clanSystem;

    @EventHandler
    public void onServerConnect(ServerConnectedEvent event) {
        ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(event.getPlayer().getUniqueId());
        Callback.of(userFuture, user -> {
            clanSystem.getRequestManager().checkForPendingRequests(user);
            clanSystem.getInviteManager().checkForPendingInvites(user);
        });
    }
}
