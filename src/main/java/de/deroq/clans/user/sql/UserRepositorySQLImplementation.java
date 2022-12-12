package de.deroq.clans.user.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Executors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class UserRepositorySQLImplementation implements UserRepository {

    private final ClanSystem clanSystem;
    private final DatabaseConnector.MySQL mySQL;
    private final String createUsersTable;
    private final String insertUser;
    private final String updateUserClan;
    private final String selectUser;
    private final String createUUIDCacheTable;
    private final String insertUUIDCache;
    private final String selectUUIDCache;

    public UserRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        // clan_users table
        this.createUsersTable = "CREATE TABLE IF NOT EXISTS clan_users(player VARCHAR(36), name VARCHAR(16), clan VARCHAR(36), PRIMARY KEY(player))";
        this.insertUser = "INSERT INTO clan_users(player, name, clan) VALUES (?, ?, ?)";
        this.updateUserClan = "UPDATE clan_users SET clan = ? WHERE player = ?";
        this.selectUser = "SELECT * FROM clan_users WHERE player = ?";

        // uuid_cache table
        this.createUUIDCacheTable = "CREATE TABLE IF NOT EXISTS uuid_cache(name VARCHAR(16), uuid VARCHAR(36), PRIMARY KEY(name))";
        this.insertUUIDCache = "INSERT INTO uuid_cache(name, uuid) VALUES (?, ?)";
        this.selectUUIDCache = "SELECT uuid FROM uuid_cache WHERE name = ?";
    }

    public UserRepository createTables() {
        mySQL.update(createUsersTable);
        mySQL.update(createUUIDCacheTable);
        return this;
    }

    @Override
    public ListenableFuture<Boolean> insertUser(AbstractUser user) {
        return mySQL.update(
                insertUser,
                user.getUuid().toString(), user.getName(), null
        );
    }

    @Override
    public synchronized ListenableFuture<AbstractUser> getUser(UUID uuid) {
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
                            clan
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
