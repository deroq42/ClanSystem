package de.deroq.clans;

import com.google.gson.Gson;
import de.deroq.clans.command.ClanCommand;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.command.subcommand.*;
import de.deroq.clans.config.Config;
import de.deroq.clans.config.MainConfig;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.database.MySQLConnector;
import de.deroq.clans.invite.InviteManager;
import de.deroq.clans.invite.sql.ClanInviteRepositorySQLImplementation;
import de.deroq.clans.listener.LoginListener;
import de.deroq.clans.listener.PlayerDisconnectListener;
import de.deroq.clans.repository.ClanDataRepository;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.repository.sql.ClanDataRepositorySQLImplementation;
import de.deroq.clans.user.sql.UserRepositorySQLImplementation;
import de.deroq.clans.user.UserManager;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Miles
 * @since 08.12.2022
 */
public class ClanSystem extends Plugin {

    @Getter
    private MainConfig mainConfig;

    @Getter
    private DatabaseConnector databaseConnector;

    @Getter
    private ClanDataRepository clanDataRepository;

    @Getter
    private ClanManager clanManager;

    @Getter
    private UserRepository userRepository;

    @Getter
    private UserManager userManager;

    @Getter
    private ClanInviteRepository clanInviteRepository;

    @Getter
    private InviteManager inviteManager;

    @Getter
    private final Map<String, ClanSubCommand> commandMap = new HashMap<>();

    public static final Pattern VALID_CLAN_NAMES = Pattern.compile("^[a-zA-Z0-9$&öäüÖÄÜ#+_\\-]{3,16}$");
    public static final Pattern VALID_CLAN_TAGS = Pattern.compile("^[a-zA-Z0-9$&öäüÖÄÜ#+_.,\\-]{2,5}$");
    public static final String PREFIX = "§7[§cClans§7] ";

    @Override
    public void onEnable() {
        loadConfigs();
        establishDatabaseConnection();
        makeInstances();
        registerListeners();
        registerCommands();
        getLogger().info("ClanSystem has been enabled.");
    }

    @Override
    public void onDisable() {
        databaseConnector.disconnect();
        getLogger().info("ClanSystem has been disabled.");
    }

    private void loadConfigs() {
        File mainConfigFile = new File("plugins/ClanSystem", "config.json");
        this.mainConfig = (MainConfig) loadConfig(mainConfigFile, MainConfig.class);
        if (mainConfig == null) {
            mainConfig = new MainConfig(mainConfigFile);
        }
        int enabledDatabases = (int) Stream.of(mainConfig.isMySQL(), mainConfig.isMongoDB(), mainConfig.isCassandra())
                .filter(b -> b)
                .count();
        if (enabledDatabases != 1) {
            throw new RuntimeException("Error while loading main config: You must use one database");
        }
    }

    private void establishDatabaseConnection() {
        if (mainConfig.isMySQL()) {
            this.databaseConnector = new MySQLConnector(this, "localhost", "clansystem", "3306", "root", "");
            databaseConnector.connect();
        }
    }

    private void makeInstances() {
        this.clanDataRepository = new ClanDataRepositorySQLImplementation(this).createTables();
        this.clanManager = new ClanManager(clanDataRepository);
        this.userRepository = new UserRepositorySQLImplementation(this).createTables();
        this.userManager = new UserManager(userRepository);
        this.clanInviteRepository = new ClanInviteRepositorySQLImplementation(this).createTables();
        this.inviteManager = new InviteManager(clanInviteRepository);
    }

    private void registerListeners() {
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        pluginManager.registerListener(this, new LoginListener(this));
        pluginManager.registerListener(this, new PlayerDisconnectListener(this));
    }

    private void registerCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ClanCommand(this));
        getCommandMap().put("create", new ClanCreateCommand(this));
        getCommandMap().put("delete", new ClanDeleteCommand(this));
        getCommandMap().put("rename", new ClanRenameCommand(this));
        getCommandMap().put("invite", new ClanInviteCommand(this));
        getCommandMap().put("join", new ClanJoinCommand(this));
        getCommandMap().put("deny", new ClanDenyCommand(this));
        getCommandMap().put("leave", new ClanLeaveCommand(this));
        getCommandMap().put("promote", new ClanPromoteCommand(this));
        getCommandMap().put("demote", new ClanDemoteCommand(this));
        getCommandMap().put("info", new ClanInfoCommand());
    }

    public Config loadConfig(File file, Class<? extends Config> aClass) {
        if (!file.getParentFile().exists() || !file.exists()) {
            return null;
        }
        try (FileReader fileReader = new FileReader(file)) {
            Config config = new Gson().fromJson(fileReader, aClass);
            config.setFile(file);
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
