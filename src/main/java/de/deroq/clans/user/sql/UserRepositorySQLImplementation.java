package de.deroq.clans.user.sql;

import com.google.common.cache.Cache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Executors;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class UserRepositorySQLImplementation implements UserRepository {

    private final ClanSystem clanSystem;
    private final String createUsersTable;
    private final String insertUser;
    private final String updateUserClan;
    private final String selectUser;
    private final String createUUIDCacheTable;
    private final String insertUUIDCache;
    private final String selectUUIDCache;

    public UserRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
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

    public UserRepositorySQLImplementation createTables() {
        clanSystem.getDatabaseConnector().getMySQL().update(createUsersTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createUUIDCacheTable);
        return this;
    }

    @Override
    public ListenableFuture<Boolean> insertUser(ClanUser user) {
        return clanSystem.getDatabaseConnector().getMySQL().update(
                insertUser,
                user.getUuid().toString(),
                user.getName(),
                null
        );
    }

    @Override
    public synchronized ListenableFuture<ClanUser> getUser(UUID uuid, Cache<UUID, ListenableFuture<ClanUser>> cache) {
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectUser, uuid.toString()), resultSet -> {
            try {
                if (resultSet.next()) {
                    String stringUuid = resultSet.getString("clan");
                    UUID clan = (stringUuid == null ? null : UUID.fromString(stringUuid));
                    ClanUser user = new ClanUser(
                            clanSystem,
                            uuid,
                            resultSet.getString("name"),
                            clan
                    );
                    cache.put(uuid, Futures.immediateFuture(user));
                    return user;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    @Override
    public ListenableFuture<Boolean> setClan(UUID player, UUID newClan) {
        return clanSystem.getDatabaseConnector().getMySQL().update(
                updateUserClan,
                (newClan == null ? null : newClan.toString()),
                player.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> cacheUUID(String name, UUID uuid) {
        return clanSystem.getDatabaseConnector().getMySQL().update(
                insertUUIDCache,
                name.toLowerCase(),
                uuid.toString()
        );
    }

    @Override
    public synchronized ListenableFuture<UUID> getUUID(String name) {
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectUUIDCache, name.toLowerCase()), resultSet -> {
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
