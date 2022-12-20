package de.deroq.clans;

import com.google.gson.Gson;
import de.deroq.clans.command.ClanChatCommand;
import de.deroq.clans.command.ClanCommand;
import de.deroq.clans.command.ClanSubCommand;
import de.deroq.clans.command.subcommand.*;
import de.deroq.clans.config.Config;
import de.deroq.clans.config.MainConfig;
import de.deroq.clans.config.MySQLConfig;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.database.sql.MySQLConnector;
import de.deroq.clans.invite.InviteManager;
import de.deroq.clans.invite.InviteManagerImplementation;
import de.deroq.clans.invite.sql.ClanInviteRepositorySQLImplementation;
import de.deroq.clans.language.LanguageManager;
import de.deroq.clans.language.LanguageManagerImplementation;
import de.deroq.clans.language.exception.LocaleLoadException;
import de.deroq.clans.listener.LoginListener;
import de.deroq.clans.listener.PlayerDisconnectListener;
import de.deroq.clans.listener.ServerConnectListener;
import de.deroq.clans.repository.ClanDataRepository;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.repository.ClanRequestRepository;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.repository.sql.ClanDataRepositorySQLImplementation;
import de.deroq.clans.request.RequestManager;
import de.deroq.clans.request.RequestManagerImplementation;
import de.deroq.clans.request.sql.ClanRequestRepositorySQLImplementation;
import de.deroq.clans.user.UserManagerImplementation;
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

/**
 * @author Miles
 * @since 08.12.2022
 */
public class ClanSystem extends Plugin {

    @Getter
    private MainConfig mainConfig;

    @Getter
    private MySQLConfig mySQLConfig;

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
    private ClanRequestRepository clanRequestRepository;

    @Getter
    private RequestManager requestManager;

    @Getter
    private LanguageManager languageManager;

    @Getter
    private final Map<String, ClanSubCommand> commandMap = new HashMap<>();

    public static final Pattern VALID_CLAN_NAMES = Pattern.compile("^[a-zA-Z0-9$&öäüÖÄÜ#+_\\-]{3,16}$");
    public static final Pattern VALID_CLAN_TAGS = Pattern.compile("^[a-zA-Z0-9$&öäüÖÄÜ#+_.,\\-]{2,5}$");
    public static final String PREFIX = "§7[§cClans§7] ";
    public static int CLAN_PLAYER_LIMIT;

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
        loadMainConfig();
        loadMySQLConfig();
    }

    private void loadMainConfig() {
        File mainConfigFile = new File("plugins/ClanSystem", "config.json");
        this.mainConfig = (MainConfig) loadConfig(mainConfigFile, MainConfig.class);
        if (mainConfig == null) {
            mainConfig = new MainConfig(mainConfigFile);
        }
        if (mainConfig.getClanPlayerLimit() > 1394) {
            throw new RuntimeException("Error while loading main config: Clan player limit is larger than 1394");
        }
        CLAN_PLAYER_LIMIT = mainConfig.getClanPlayerLimit();
    }

    private void loadMySQLConfig() {
        File mySQLConfigFile = new File("plugins/ClanSystem", "mysql.json");
        this.mySQLConfig = (MySQLConfig) loadConfig(mySQLConfigFile, MySQLConfig.class);
        if (mySQLConfig == null) {
            mySQLConfig = new MySQLConfig(mySQLConfigFile);
        }
    }

    private void establishDatabaseConnection() {
        this.databaseConnector = new MySQLConnector(
                this,
                mySQLConfig.getHost(),
                mySQLConfig.getDatabase(),
                mySQLConfig.getPort(),
                mySQLConfig.getUsername(),
                mySQLConfig.getPassword()
        );
        databaseConnector.connect();
    }

    private void makeInstances() {
        this.clanDataRepository = new ClanDataRepositorySQLImplementation(this).createTables();
        this.clanManager = new ClanManagerImplementation(this, clanDataRepository);
        this.userRepository = new UserRepositorySQLImplementation(this).createTables();
        this.userManager = new UserManagerImplementation(this, userRepository);
        this.clanInviteRepository = new ClanInviteRepositorySQLImplementation(this).createTables();
        this.inviteManager = new InviteManagerImplementation(this, clanInviteRepository);
        this.clanRequestRepository = new ClanRequestRepositorySQLImplementation(this).createTable();
        this.requestManager = new RequestManagerImplementation(this, clanRequestRepository);
        this.languageManager = new LanguageManagerImplementation(new File("plugins/ClanSystem/locales"), getLogger());
        try {
            languageManager.loadLocales(true);
            languageManager.startRefreshing(this);
        } catch (LocaleLoadException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerListeners() {
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        pluginManager.registerListener(this, new LoginListener(this));
        pluginManager.registerListener(this, new PlayerDisconnectListener(this));
        pluginManager.registerListener(this, new ServerConnectListener(this));
    }

    private void registerCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ClanCommand(this));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ClanChatCommand(this));
        getCommandMap().put("create", new ClanCreateCommand(this));
        getCommandMap().put("delete", new ClanDeleteCommand(this));
        getCommandMap().put("rename", new ClanRenameCommand(this));
        getCommandMap().put("invite", new ClanInviteCommand(this));
        getCommandMap().put("join", new ClanJoinCommand(this));
        getCommandMap().put("deny", new ClanDenyCommand(this));
        getCommandMap().put("leave", new ClanLeaveCommand(this));
        getCommandMap().put("promote", new ClanPromoteCommand(this));
        getCommandMap().put("demote", new ClanDemoteCommand(this));
        getCommandMap().put("info", new ClanInfoCommand(this));
        getCommandMap().put("tinfo", new ClanTagInfoCommand(this));
        getCommandMap().put("ninfo", new ClanNameInfoCommand(this));
        getCommandMap().put("uinfo", new ClanUserInfoCommand(this));
        getCommandMap().put("denyall", new ClanDenyAllCommand(this));
        getCommandMap().put("request", new ClanRequestCommand(this));
        getCommandMap().put("accept", new ClanAcceptCommand(this));
        getCommandMap().put("decline", new ClanDeclineCommand(this));
        getCommandMap().put("requests", new ClanRequestsCommand(this));
        getCommandMap().put("acceptall", new ClanAcceptAllCommand(this));
        getCommandMap().put("declineall", new ClanDeclineAllCommand(this));
        getCommandMap().put("kick", new ClanKickCommand(this));
        getCommandMap().put("setlanguage", new ClanSetLanguageCommand(this));
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
