package de.deroq.clans.request.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.model.AbstractClan;
import de.deroq.clans.repository.ClanRequestRepository;
import de.deroq.clans.user.AbstractUser;
import de.deroq.clans.util.Executors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class ClanRequestRepositorySQLImplementation implements ClanRequestRepository {

    private final DatabaseConnector.MySQL mySQL;
    private final String createRequestsTable;
    private final String insertRequest;
    private final String deleteRequest;
    private final String selectRequestsByClan;
    private final String deleteRequests;

    public ClanRequestRepositorySQLImplementation(ClanSystem clanSystem) {
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        this.createRequestsTable = "CREATE TABLE IF NOT EXISTS clan_requests(clan VARCHAR(36), player VARCHAR(36), PRIMARY KEY(clan, player))";
        this.insertRequest = "INSERT INTO clan_requests(clan, player) VALUES (?, ?)";
        this.deleteRequest = "DELETE FROM clan_requests WHERE clan = ? AND player = ?";
        this.selectRequestsByClan = "SELECT player FROM clan_requests WHERE clan = ?";
        this.deleteRequests = "DELETE FROM clan_requests WHERE clan = ?";
    }

    public ClanRequestRepository createTable() {
        mySQL.update(createRequestsTable);
        return this;
    }

    @Override
    public ListenableFuture<Boolean> insertRequest(AbstractClan clan, AbstractUser user) {
        return mySQL.update(
                insertRequest,
                clan.getClanId().toString(), user.getUuid().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteRequest(AbstractClan clan, AbstractUser user) {
        return mySQL.update(
                deleteRequest,
                clan.getClanId().toString(), user.getUuid().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteRequests(AbstractClan clan) {
        return mySQL.update(
                deleteRequests,
                clan.getClanId().toString()
        );
    }

    @Override
    public ListenableFuture<Set<UUID>> getRequests(UUID clan) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectRequestsByClan,
                clan.toString()
        );
        return Futures.transform(future, resultSet -> {
            try {
                Set<UUID> requests = ConcurrentHashMap.newKeySet();
                while (resultSet.next()) {
                    UUID player = UUID.fromString(resultSet.getString("player"));
                    requests.add(player);
                }
                return requests;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, Executors.asyncExecutor());
    }
}
