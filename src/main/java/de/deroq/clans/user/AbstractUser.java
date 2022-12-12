package de.deroq.clans.user;

import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.model.AbstractClan;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * @author Miles
 * @since 12.12.2022
 */
public interface AbstractUser {

    void sendMessage(String message);

    ListenableFuture<AbstractClan> getClan();

    boolean isOnline();

    ProxiedPlayer getPlayer();

    UUID getUuid();

    String getName();

    void setClan(UUID clan);
}
