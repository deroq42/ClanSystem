package de.deroq.clans.invite.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.database.DatabaseConnector;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.util.Executors;
import de.deroq.clans.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class ClanInviteRepositorySQLImplementation implements ClanInviteRepository {

    private final DatabaseConnector.MySQL mySQL;
    private final String createInvitesTable;
    private final String insertInvite;
    private final String deleteInvite;
    private final String deleteInvitesByPlayer;
    private final String deleteInvitesByClan;
    private final String selectInvites;

    public ClanInviteRepositorySQLImplementation(ClanSystem clanSystem) {
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        this.createInvitesTable = "CREATE TABLE IF NOT EXISTS clan_invites(player VARCHAR(36), clan VARCHAR(36), inviter VARCHAR(36), PRIMARY KEY(player, clan))";
        this.insertInvite = "INSERT INTO clan_invites(player, clan, inviter) VALUES (?, ?, ?)";
        this.deleteInvite = "DELETE FROM clan_invites WHERE player = ? AND clan = ?";
        this.deleteInvitesByPlayer = "DELETE FROM clan_invites WHERE player = ?";
        this.deleteInvitesByClan = "DELETE FROM clan_invites WHERE clan = ?";
        this.selectInvites = "SELECT clan, inviter FROM clan_invites WHERE player = ?";
    }

    public ClanInviteRepository createTables() {
        mySQL.update(createInvitesTable);
        return this;
    }

    @Override
    public ListenableFuture<Boolean> insertInvite(UUID invited, UUID clan, UUID inviter) {
        return mySQL.update(
                insertInvite,
                invited.toString(), clan.toString(), inviter.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteInvite(UUID player, UUID clan) {
        return mySQL.update(
                deleteInvite,
                player.toString(), clan.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteInvitesByPlayer(UUID player) {
        return mySQL.update(
                deleteInvitesByPlayer,
                player.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteInvitesByClan(UUID clan) {
        return mySQL.update(
                deleteInvitesByClan,
                clan.toString());
    }

    @Override
    public ListenableFuture<Set<Pair<UUID, UUID>>> getInvites(UUID player) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectInvites,
                player.toString()
        );
        return Futures.transform(future, resultSet -> {
            try {
                Set<Pair<UUID, UUID>> invites = new HashSet<>();
                while (resultSet.next()) {
                    UUID clan = UUID.fromString(resultSet.getString("clan"));
                    UUID inviter = UUID.fromString(resultSet.getString("inviter"));
                    invites.add(Pair.of(clan, inviter));
                }
                return invites;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, Executors.asyncExecutor());
    }
}
