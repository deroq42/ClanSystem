package de.deroq.clans.listener;

import de.deroq.clans.ClanSystem;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final ClanSystem clanSystem;

    @EventHandler
    public void onLogin(LoginEvent event) {
        clanSystem.getUserManager().createUser(
                event.getConnection().getUniqueId(),
                event.getConnection().getName()
        );
    }
}
