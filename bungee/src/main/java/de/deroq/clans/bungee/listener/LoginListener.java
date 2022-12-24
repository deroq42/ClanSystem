package de.deroq.clans.bungee.listener;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Callback;
import de.deroq.clans.bungee.ClanSystem;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final ClanSystem clanSystem;

    @EventHandler
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        String name = event.getConnection().getName();
        ListenableFuture<AbstractClanUser> userFuture = clanSystem.getUserManager().getUser(uuid);
        Callback.of(userFuture, user -> {
            if (user == null) {
                clanSystem.getUserManager().createUser(uuid, name);
                clanSystem.getUserManager().cacheUuid(name, uuid);
            } else {
                clanSystem.getUserManager().cacheOnlineUser(user);
            }
        });
    }
}
