package de.deroq.clans.listener;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
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
        ListenableFuture<AbstractUser> userFuture = clanSystem.getUserManager().getUser(uuid);
        Callback.of(userFuture, user -> {
            if (user == null) {
                clanSystem.getUserManager().createUser(clanSystem, uuid, name);
                clanSystem.getUserManager().cacheUuid(name, uuid);
            } else {
                clanSystem.getUserManager().cacheOnlineUser(user);
            }
        });
    }
}
