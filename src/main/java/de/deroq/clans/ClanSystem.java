package de.deroq.clans;

import com.google.gson.Gson;
import de.deroq.clans.config.Config;
import de.deroq.clans.config.MainConfig;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.database.MySQLConnector;
import de.deroq.clans.listener.LoginListener;
import de.deroq.clans.repository.ClanRepository;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.repository.sql.ClanRepositorySQLImplementation;
import de.deroq.clans.repository.sql.UserRepositorySQLImplementation;
import de.deroq.clans.user.UserManager;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private ClanRepository clanRepository;

    @Getter
    private ClanManager clanManager;

    @Getter
    private UserRepository userRepository;

    @Getter
    private UserManager userManager;

    @Override
    public void onEnable() {
        loadConfigs();
        establishDatabaseConnection();
        makeInstances();
        registerListeners();
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
        this.clanRepository = new ClanRepositorySQLImplementation(this).createTables();
        this.clanManager = new ClanManager(clanRepository);
        this.userRepository = new UserRepositorySQLImplementation(this).createTables();
        this.userManager = new UserManager(userRepository);
    }

    private void registerListeners() {
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        pluginManager.registerListener(this, new LoginListener(this));
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
