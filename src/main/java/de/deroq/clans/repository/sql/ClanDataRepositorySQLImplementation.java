package de.deroq.clans.repository.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.*;
import de.deroq.clans.model.Clan;
import de.deroq.clans.repository.ClanDataRepository;
import de.deroq.clans.user.ClanUser;
import de.deroq.clans.util.Executors;
import lombok.RequiredArgsConstructor;

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
        // clans table
        this.createClansTable = "CREATE TABLE IF NOT EXISTS clans(id VARCHAR(36), clanname VARCHAR(16), clantag VARCHAR(5), members VARCHAR(1320), PRIMARY KEY(id))";
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

    public ClanDataRepositorySQLImplementation createTables() {
        clanSystem.getDatabaseConnector().getMySQL().update(createClansTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createClansByNameTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createClansByTagTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createClansByPlayerTable);
        return this;
    }

    @Override
    public ListenableFuture<Clan> createClan(UUID player, Clan clan) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClan,
                clan.getClanId().toString(),
                clan.getClanName(),
                clan.getClanTag(),
                player.toString() + "=" + Clan.Group.LEADER + ";"
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanByName,
                clan.getClanName().toLowerCase(),
                clan.getClanId().toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanByTag,
                clan.getClanTag().toLowerCase(),
                clan.getClanId().toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanByPlayer,
                player.toString(),
                clan.getClanId().toString()
        );
        return Futures.immediateFuture(clan);
    }

    @Override
    public ListenableFuture<Boolean> deleteClan(Clan clan) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClanByName,
                clan.getClanName().toLowerCase()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClanByTag,
                clan.getClanTag().toLowerCase()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClanByPlayerClan,
                clan.getClanId().toString()
        );
        return clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClan,
                clan.getClanId().toString()
        );
    }

    @Override
    public ListenableFuture<Boolean> renameClan(Clan clan, String oldName, String oldTag) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                updateClanNameAndTag,
                clan.getClanName(),
                clan.getClanTag(),
                clan.getClanId().toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                updateClanByName,
                clan.getClanName().toLowerCase(),
                oldName.toLowerCase()
        );
        return clanSystem.getDatabaseConnector().getMySQL().update(
                updateClanByTag,
                clan.getClanTag().toLowerCase(),
                oldTag.toLowerCase()
        );
    }

    @Override
    public ListenableFuture<Boolean> joinClan(ClanUser user, Clan clan) {
        StringBuilder members = new StringBuilder();
        for (Map.Entry<UUID, Clan.Group> entry : clan.getMembers().entrySet()) {
            members.append(entry.getKey().toString()).append("=")
                    .append(entry.getValue()).append(";");
        }
        clanSystem.getUserManager().setClan(
                user,
                clan.getClanId()
        );
        clanSystem.getInviteManager().removeInvite(
                user.getUuid(),
                clan.getClanId()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanByPlayer,
                user.getUuid().toString(),
                clan.getClanId().toString()
        );
        return clanSystem.getDatabaseConnector().getMySQL().update(
                updateClanMembers,
                members.toString(),
                clan.getClanId().toString()
        );
    }

    @Override
    public synchronized ListenableFuture<Clan> getClanById(UUID id) {
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectClanById, id.toString()), resultSet -> {
            try {
                if (resultSet.next()) {
                    return new Clan(
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
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectClanByName, clanName.toLowerCase()), resultSet -> {
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
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectClanByTag, clanTag.toLowerCase()), resultSet -> {
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
        return Futures.transform(clanSystem.getDatabaseConnector().getMySQL().query(selectClanByPlayer, player.toString()), resultSet -> {
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

    private Map<UUID, Clan.Group> parseMembers(String s) {
        String[] args = s.split(";");
        Map<UUID, Clan.Group> members = new ConcurrentHashMap<>();
        Arrays.stream(args)
                .map(member -> member.split("="))
                .forEach(uuidRank -> members.put(UUID.fromString(uuidRank[0]), Clan.Group.valueOf(uuidRank[1])));
        return members;
    }
}