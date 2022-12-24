package de.deroq.clans.bukkit.user;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.database.DatabaseConnector;
import de.deroq.clans.api.repository.ClanUserRepository;
import de.deroq.clans.api.util.Executors;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bukkit.ClanSystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Miles
 * @since 23.12.2022
 */
public class ClanUserRepositorySQLImplementation implements ClanUserRepository {

    private final ClanSystem clanSystem;
    private final DatabaseConnector.MySQL mySQL;
    private final String insertUser;
    private final String updateUserClan;
    private final String selectUser;
    private final String updateUserLocale;
    private final String insertUUIDCache;
    private final String selectUUIDCache;

    public ClanUserRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        // clan_users table
        this.insertUser = "INSERT INTO clan_users(player, name, clan, locale) VALUES (?, ?, ?, ?)";
        this.updateUserClan = "UPDATE clan_users SET clan = ? WHERE player = ?";
        this.selectUser = "SELECT * FROM clan_users WHERE player = ?";
        this.updateUserLocale = "UPDATE clan_users SET locale = ? WHERE player = ?";

        // uuid_cache table
        this.insertUUIDCache = "INSERT INTO uuid_cache(name, uuid) VALUES (?, ?)";
        this.selectUUIDCache = "SELECT uuid FROM uuid_cache WHERE name = ?";
    }

    @Override
    public ListenableFuture<Boolean> insertUser(AbstractClanUser user) {
        return mySQL.update(
                insertUser,
                user.getUuid().toString(), user.getName(), null, user.getLocale().toLanguageTag()
        );
    }

    @Override
    public synchronized ListenableFuture<AbstractClanUser> getUser(UUID uuid) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectUser,
                uuid.toString()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    String stringUuid = resultSet.getString("clan");
                    UUID clan = (stringUuid == null ? null : UUID.fromString(stringUuid));
                    return new ClanUser(
                            clanSystem,
                            uuid,
                            resultSet.getString("name"),
                            clan,
                            Locale.forLanguageTag(resultSet.getString("locale"))
                    );
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    @Override
    public ListenableFuture<Boolean> setClan(UUID player, UUID newClan) {
        return mySQL.update(
                updateUserClan,
                (newClan == null ? null : newClan.toString()), player.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> updateLocale(AbstractClanUser user, Locale locale) {
        return mySQL.update(
                updateUserLocale,
                locale.toLanguageTag(), user.getUuid().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> cacheUUID(String name, UUID uuid) {
        return mySQL.update(
                insertUUIDCache,
                name.toLowerCase(), uuid.toString()
        );
    }

    @Override
    public synchronized ListenableFuture<UUID> getUUID(String name) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectUUIDCache,
                name.toLowerCase()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("uuid"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }
}
