package de.deroq.clans.repository.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.repository.UserRepository;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Executors;

import java.sql.SQLException;
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

    public UserRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        this.createUsersTable = "CREATE TABLE IF NOT EXISTS clan_users(player VARCHAR(36), name VARCHAR(16), clan VARCHAR(36), PRIMARY KEY(player))";
        this.insertUser = "INSERT INTO clan_users(player, name, clan) VALUES (?, ?, ?)";
        this.updateUserClan = "UPDATE clan_users SET clan = ? WHERE player = ?";
        this.selectUser = "SELECT * FROM clan_users WHERE player = ?";
    }

    public UserRepositorySQLImplementation createTables() {
        clanSystem.getDatabaseConnector().getMySQL().update(createUsersTable);
        return this;
    }

    @Override
    public ListenableFuture<ClanUser> insertUser(ClanUser user) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertUser,
                user.getUuid().toString(),
                user.getName(),
                null
        );
        return Futures.immediateFuture(user);
    }

    @Override
    public ListenableFuture<ClanUser> getUser(UUID uuid) {
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectUser, uuid.toString()), resultSet -> {
            try {
                if (resultSet.next()) {
                    String stringUuid = resultSet.getString("clan");
                    UUID clan = (stringUuid == null ? null : UUID.fromString(stringUuid));
                    return new ClanUser(
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
    public void updateClan(UUID player, UUID clan) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                updateUserClan,
                clan,
                player
        );
    }
}
