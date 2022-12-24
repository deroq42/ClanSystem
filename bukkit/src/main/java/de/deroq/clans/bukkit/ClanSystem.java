package de.deroq.clans.bukkit;

import com.google.gson.Gson;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.ClanAPI;
import de.deroq.clans.api.database.DatabaseConnector;
import de.deroq.clans.api.language.LanguageManager;
import de.deroq.clans.api.repository.ClanDataRepository;
import de.deroq.clans.api.repository.ClanInviteRepository;
import de.deroq.clans.api.repository.ClanRequestRepository;
import de.deroq.clans.api.repository.ClanUserRepository;
import de.deroq.clans.bukkit.api.ClanAPIImplementation;
import de.deroq.clans.bukkit.config.Config;
import de.deroq.clans.bukkit.config.MySQLConfig;
import de.deroq.clans.bukkit.database.MySQLConnector;
import de.deroq.clans.bukkit.invite.ClanInviteRepositorySQLImplementation;
import de.deroq.clans.bukkit.language.LanguageManagerImplementation;
import de.deroq.clans.bukkit.request.ClanRequestRepositorySQLImplementation;
import de.deroq.clans.bukkit.user.ClanUserRepositorySQLImplementation;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Miles
 * @since 23.12.2022
 */
public class ClanSystem extends JavaPlugin {

    @Getter
    private MySQLConfig mySQLConfig;

    @Getter
    private DatabaseConnector databaseConnector;

    @Getter
    private ClanDataRepository dataRepository;

    @Getter
    private ClanInviteRepository inviteRepository;

    @Getter
    private ClanRequestRepository requestRepository;

    @Getter
    private ClanUserRepository userRepository;

    @Getter
    private LanguageManager languageManager;

    @Getter
    private ClanAPI clanAPI;

    @Override
    public void onEnable() {
        loadMySQLConfig();
        establishDatabaseConnection();
        makeInstances();

        clanAPI.getClanByTag("esd", uuid -> clanAPI.getClanById(uuid, clan -> System.out.println(clan.getInfo().getUsersWithGroup(AbstractClan.Group.LEADER))));
    }

    @Override
    public void onDisable() {
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
        this.dataRepository = new ClanDataRepositorySQLImplementation(this);
        this.inviteRepository = new ClanInviteRepositorySQLImplementation(this);
        this.requestRepository = new ClanRequestRepositorySQLImplementation(this);
        this.userRepository = new ClanUserRepositorySQLImplementation(this);
        this.languageManager = new LanguageManagerImplementation(this, new File("/plugins/ClanSystem/locales"));
        languageManager.loadLocales(true);
        languageManager.startRefreshing();
        this.clanAPI = new ClanAPIImplementation(this);
        getLogger().info("ClanAPI has been initialized.");
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
