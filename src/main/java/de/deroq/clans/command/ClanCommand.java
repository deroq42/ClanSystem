package de.deroq.clans.command;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractClanUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class ClanCommand extends Command {

    private final ClanSystem clanSystem;

    public ClanCommand(ClanSystem clanSystem) {
        super("clan");
        this.clanSystem = clanSystem;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            return;
        }
        if (args.length == 0) {
            return;
        }
        ClanSubCommand subCommand = clanSystem.getCommandMap().get(args[0].toLowerCase());
        if (subCommand == null) {
            // Send help.
            return;
        }
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(((ProxiedPlayer) commandSender).getUniqueId());
        StringBuilder argsBuilder = new StringBuilder();
        Arrays.stream(args).skip(1).forEach(s -> argsBuilder.append(s).append(" "));
        args = argsBuilder.toString().trim().split(" ");
        String[] finalArgs = args;
        AbstractClanUser user = clanSystem.getUserManager().getOnlineUser(player.getUniqueId());
        if (user == null) {
            commandSender.sendMessage(TextComponent.fromLegacyText("§cBefehl konnte nicht ausgeführt wurden, rejoin und versuch es nochmal"));
            return;
        }
        subCommand.run(user, finalArgs);
    }
}
