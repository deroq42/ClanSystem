package de.deroq.clans.invite.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.ClanSystem;
import de.deroq.clans.repository.ClanInviteRepository;
import de.deroq.clans.util.Executors;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class ClanInviteRepositorySQLImplementation implements ClanInviteRepository {

    private final ClanSystem clanSystem;
    private final String createInvitesTable;
    private final String insertInvite;
    private final String deleteInvite;
    private final String selectInvites;

    public ClanInviteRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        // clan_invites table
        this.createInvitesTable = "CREATE TABLE IF NOT EXISTS clan_invites(player VARCHAR(36), clan VARCHAR(36), inviter VARCHAR(36))";
        this.insertInvite = "INSERT INTO clan_invites(player, clan, inviter) VALUES (?, ?, ?)";
        this.deleteInvite = "DELETE FROM clan_invites WHERE player = ? AND clan = ?";
        this.selectInvites = "SELECT clan FROM clan_invites WHERE player = ?";
    }

    public ClanInviteRepositorySQLImplementation createTables() {
        clanSystem.getDatabaseConnector().getMySQL().update(createInvitesTable);
        return this;
    }

    @Override
    public ListenableFuture<Boolean> insertInvite(UUID invited, UUID clan, UUID inviter) {
        return clanSystem.getDatabaseConnector().getMySQL().update(
                insertInvite,
                invited.toString(),
                clan.toString(),
                inviter.toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> deleteInvite(UUID player, UUID clan) {
        return clanSystem.getDatabaseConnector().getMySQL().update(
                deleteInvite,
                player.toString(),
                clan.toString()
        );
    }

    @Override
    public ListenableFuture<Set<UUID>> getInvites(UUID player) {
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectInvites, player.toString()), resultSet -> {
            try {
                Set<UUID> invites = new HashSet<>();
                while (resultSet.next()) {
                    invites.add(UUID.fromString(resultSet.getString("clan")));
                }
                return invites;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, Executors.asyncExecutor());
    }
}
