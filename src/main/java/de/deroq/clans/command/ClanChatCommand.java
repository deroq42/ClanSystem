package de.deroq.clans.command;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class ClanChatCommand extends Command {

    private final ClanSystem clanSystem;

    public ClanChatCommand(ClanSystem clanSystem) {
        super("clanchat", null, "cc");
        this.clanSystem = clanSystem;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            return;
        }
        AbstractUser user = clanSystem.getUserManager().getOnlineUser(((ProxiedPlayer) commandSender).getUniqueId());
        if (user == null) {
            commandSender.sendMessage(TextComponent.fromLegacyText("§cBefehl konnte nicht ausgeführt wurden, rejoin und versuch es nochmal"));
            return;
        }
        if (args.length == 0) {
            return;
        }
        Callback.of(user.getClan(), currentClan -> {
            if (currentClan == null) {
                user.sendMessage("no-clan");
                return;
            }
            StringBuilder messageBuilder = new StringBuilder();
            for (String arg : args) {
                messageBuilder.append(arg).append(" ");
            }
            currentClan.chat(user, messageBuilder.toString().trim());
        });
    }
}
