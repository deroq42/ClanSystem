package de.deroq.clans.bungee.command;

import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.api.user.AbstractClanUser;
import net.md_5.bungee.api.CommandSender;
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
        AbstractClanUser user = clanSystem.getUserManager().getOnlineUser(((ProxiedPlayer) commandSender).getUniqueId());
        if (user == null) {
            commandSender.sendMessage(TextComponent.fromLegacyText("§cBefehl konnte nicht ausgeführt wurden, rejoin und versuch es nochmal"));
            return;
        }
        if (args.length == 0) {
            user.sendMessage("clan-help-page1");
            return;
        }
        ClanSubCommand subCommand = clanSystem.getCommandMap().get(args[0].toLowerCase());
        if (subCommand == null) {
            user.sendMessage("clan-help-page1");
            return;
        }
        StringBuilder argsBuilder = new StringBuilder();
        Arrays.stream(args).skip(1).forEach(s -> argsBuilder.append(s).append(" "));
        args = argsBuilder.toString().trim().split(" ");
        String[] finalArgs = args;
        subCommand.run(user, finalArgs);
    }
}
