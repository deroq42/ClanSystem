package de.deroq.clans.listener;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
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
        ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(event.getPlayer().getUniqueId());
        Callback.of(userFuture, user -> {
            clanSystem.getRequestManager().checkForPendingRequests(user);
            clanSystem.getInviteManager().checkForPendingInvites(user);
        });
    }
}
