package de.deroq.clans.bungee;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.api.AbstractClan;
import de.deroq.clans.api.database.DatabaseConnector;
import de.deroq.clans.api.repository.ClanDataRepository;
import de.deroq.clans.api.user.AbstractClanUser;
import de.deroq.clans.api.util.Executors;
import de.deroq.clans.bungee.ClanSystem;
import de.deroq.clans.bungee.Clan;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Miles
 * @since 08.12.2022
 */
@RequiredArgsConstructor
public class ClanDataRepositorySQLImplementation implements ClanDataRepository {

    private final ClanSystem clanSystem;
    private final DatabaseConnector.MySQL mySQL;
    private final String createClansTable;
    private final String insertClan;
    private final String deleteClan;
    private final String selectClanById;
    private final String updateClanNameAndTag;
    private final String updateClanMembers;
    private final String createClansByNameTable;
    private final String insertClanByName;
    private final String deleteClanByName;
    private final String updateClanByName;
    private final String selectClanByName;
    private final String createClansByTagTable;
    private final String insertClanByTag;
    private final String deleteClanByTag;
    private final String updateClanByTag;
    private final String selectClanByTag;
    private final String createClansByPlayerTable;
    private final String insertClanByPlayer;
    private final String deleteClanByPlayerClan;
    private final String deleteClanByPlayerUUID;
    private final String selectClanByPlayer;

    public ClanDataRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        this.mySQL = clanSystem.getDatabaseConnector().getMySQL();
        // clans table
        this.createClansTable = "CREATE TABLE IF NOT EXISTS clans(id VARCHAR(36), clanname VARCHAR(16), clantag VARCHAR(5), members TEXT, PRIMARY KEY(id))";
        this.insertClan = "INSERT INTO clans(id, clanname, clantag, members) VALUES (?, ?, ?, ?)";
        this.deleteClan = "DELETE FROM clans WHERE id = ?";
        this.selectClanById = "SELECT * FROM clans WHERE id = ?";
        this.updateClanNameAndTag = "UPDATE clans SET clanname = ?, clantag = ? WHERE id = ?";
        this.updateClanMembers = "UPDATE clans SET members = ? WHERE id = ?";

        // clans_by_name table
        this.createClansByNameTable = "CREATE TABLE IF NOT EXISTS clans_by_name(name VARCHAR(16), clan VARCHAR(36), PRIMARY KEY(name))";
        this.insertClanByName = "INSERT INTO clans_by_name(name, clan) VALUES (?, ?)";
        this.deleteClanByName = "DELETE FROM clans_by_name WHERE name = ?";
        this.updateClanByName = "UPDATE clans_by_name SET name = ? WHERE name = ?";
        this.selectClanByName = "SELECT clan FROM clans_by_name WHERE name = ?";

        // clans_by_tag table
        this.createClansByTagTable = "CREATE TABLE IF NOT EXISTS clans_by_tag(tag VARCHAR(5), clan VARCHAR(36), PRIMARY KEY(tag))";
        this.insertClanByTag = "INSERT INTO clans_by_tag(tag, clan) VALUES (?, ?)";
        this.deleteClanByTag = "DELETE FROM clans_by_tag WHERE tag = ?";
        this.updateClanByTag = "UPDATE clans_by_tag SET tag = ? WHERE tag = ?";
        this.selectClanByTag = "SELECT clan FROM clans_by_tag WHERE tag = ?";

        // clans_by_player table
        this.createClansByPlayerTable = "CREATE TABLE IF NOT EXISTS clans_by_player(player VARCHAR(36), clan VARCHAR(36))";
        this.insertClanByPlayer = "INSERT INTO clans_by_player(player, clan) VALUES (?, ?)";
        this.deleteClanByPlayerClan = "DELETE FROM clans_by_player WHERE clan = ?";
        this.deleteClanByPlayerUUID = "DELETE FROM clans_by_player WHERE player = ?";
        this.selectClanByPlayer = "SELECT clan FROM clans_by_player WHERE player = ?";
    }

    public ClanDataRepository createTables() {
        mySQL.update(createClansTable);
        mySQL.update(createClansByNameTable);
        mySQL.update(createClansByTagTable);
        mySQL.update(createClansByPlayerTable);
        return this;
    }

    @Override
    public ListenableFuture<AbstractClan> createClan(UUID player, AbstractClan clan) {
        mySQL.update(
                insertClan,
                clan.getClanId().toString(), clan.getClanName(), clan.getClanTag(), player.toString() + "=" + Clan.Group.LEADER + ";"
        );
        mySQL.update(
                insertClanByName,
                clan.getClanName().toLowerCase(), clan.getClanId().toString()
        );
        mySQL.update(
                insertClanByTag,
                clan.getClanTag().toLowerCase(), clan.getClanId().toString()
        );
        mySQL.update(
                insertClanByPlayer,
                player.toString(), clan.getClanId().toString()
        );
        return Futures.immediateFuture(clan);
    }

    @Override
    public ListenableFuture<Boolean> deleteClan(AbstractClan clan) {
        mySQL.update(
                deleteClanByName,
                clan.getClanName().toLowerCase()
        );
        mySQL.update(
                deleteClanByTag,
                clan.getClanTag().toLowerCase()
        );
        mySQL.update(
                deleteClanByPlayerClan,
                clan.getClanId().toString()
        );
        return mySQL.update(
                deleteClan,
                clan.getClanId().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> renameClan(AbstractClan clan, String oldName, String oldTag) {
        mySQL.update(
                updateClanNameAndTag,
                clan.getClanName(), clan.getClanTag(), clan.getClanId().toString()
        );
        mySQL.update(
                updateClanByName,
                clan.getClanName().toLowerCase(), oldName.toLowerCase()
        );
        return mySQL.update(
                updateClanByTag,
                clan.getClanTag().toLowerCase(), oldTag.toLowerCase()
        );
    }

    @Override
    public ListenableFuture<Boolean> joinClan(AbstractClanUser user, AbstractClan clan) {
        mySQL.update(
                insertClanByPlayer,
                user.getUuid().toString(), clan.getClanId().toString()
        );
        return updateMembers(clan);
    }

    @Override
    public ListenableFuture<Boolean> leaveClan(AbstractClanUser user, AbstractClan clan) {
        mySQL.update(
                deleteClanByPlayerUUID,
                user.getUuid().toString()
        );
        return updateMembers(clan);
    }

    @Override
    public ListenableFuture<Boolean> updateMembers(AbstractClan clan) {
        StringBuilder members = new StringBuilder();
        for (Map.Entry<UUID, AbstractClan.Group> entry : clan.getMembers().entrySet()) {
            members.append(entry.getKey().toString()).append("=")
                    .append(entry.getValue()).append(";");
        }
        return mySQL.update(
                updateClanMembers,
                members.toString(), clan.getClanId().toString()
        );
    }

    @Override
    public synchronized ListenableFuture<AbstractClan> getClanById(UUID id) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectClanById,
                id.toString()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    return new Clan(
                            clanSystem,
                            id,
                            resultSet.getString("clanname"),
                            resultSet.getString("clantag"),
                            parseMembers(resultSet.getString("members"))
                    );
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    @Override
    public synchronized ListenableFuture<UUID> getClanByName(String clanName) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectClanByName,
                clanName.toLowerCase()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("clan"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    @Override
    public synchronized ListenableFuture<UUID> getClanByTag(String clanTag) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectClanByTag,
                clanTag.toLowerCase()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("clan"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    @Override
    public ListenableFuture<UUID> getClanByPlayer(UUID player) {
        ListenableFuture<ResultSet> future = mySQL.query(
                selectClanByPlayer,
                player.toString()
        );
        return Futures.transform(future, resultSet -> {
            try {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("clan"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, Executors.asyncExecutor());
    }

    private Map<UUID, AbstractClan.Group> parseMembers(String s) {
        String[] args = s.split(";");
        Map<UUID, AbstractClan.Group> members = new ConcurrentHashMap<>();
        Arrays.stream(args)
                .map(member -> member.split("="))
                .forEach(uuidRank -> members.put(UUID.fromString(uuidRank[0]), AbstractClan.Group.valueOf(uuidRank[1])));
        return members;
    }
}
