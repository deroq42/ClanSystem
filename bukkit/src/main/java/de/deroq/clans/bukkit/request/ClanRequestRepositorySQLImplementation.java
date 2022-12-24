package de.deroq.clans.bukkit.request;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.database.DatabaseConnector;
import de.deroq.clans.api.util.Executors;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.repository.ClanRequestRepository;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.bukkit.ClanSystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class ClanRequestRepositorySQLImplementation implements ClanRequestRepository {

    private final DatabaseConnector.MySQL mySQL;
    private final String insertRequest;
    private final String deleteRequest;
    private final String selectRequestsByClan;

    public ClanRequestRepositorySQLImplementation(ClanSystem clanSystem) {
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        this.insertRequest = "INSERT INTO clan_requests(clan, player) VALUES (?, ?)";
        this.deleteRequest = "DELETE FROM clan_requests WHERE clan = ? AND player = ?";
        this.selectRequestsByClan = "SELECT player FROM clan_requests WHERE clan = ?";
    }

    @Override
    public ListenableFuture<Boolean> insertRequest(AbstractClan clan, AbstractClanUser user) {
        return mySQL.update(
                insertRequest,
                clan.getClanId().toString(), user.getUuid().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteRequest(AbstractClan clan, AbstractClanUser user) {
        return mySQL.update(
                deleteRequest,
                clan.getClanId().toString(), user.getUuid().toString()
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
