package de.deroq.clans.command;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
            // Send help.
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
        Callback.of(clanSystem.getUserManager().getUser(player.getUniqueId()), user -> {
            if (user == null) {
                throw new RuntimeException("Error while executing clan command: User could not be loaded");
            }
            subCommand.run(user, finalArgs);
        });
    }
}
