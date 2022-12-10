package de.deroq.clans.repository.sql;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import de.deroq.clans.*;
import de.deroq.clans.model.Clan;
import de.deroq.clans.repository.ClanRepository;
import de.deroq.clans.util.Executors;
import de.deroq.clans.util.Pair;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Miles
 * @since 08.12.2022
 */
@RequiredArgsConstructor
public class ClanRepositorySQLImplementation implements ClanRepository {

    private final ClanSystem clanSystem;
    private final String createClansTable;
    private final String insertClan;
    private final String deleteClan;
    private final String selectClanById;
    private final String createClansByNameTable;
    private final String insertClanName;
    private final String deleteClanName;
    private final String updateClanName;
    private final String selectClanByName;
    private final String createClansByTagTable;
    private final String insertClanTag;
    private final String deleteClanTag;
    private final String updateClanTag;
    private final String selectClanByTag;

    public ClanRepositorySQLImplementation(ClanSystem clanSystem) {
        this.clanSystem = clanSystem;
        // clans table
        this.createClansTable = "CREATE TABLE IF NOT EXISTS clans(id VARCHAR(36), clanname VARCHAR(16), clantag VARCHAR(5), members VARCHAR(1320), PRIMARY KEY(id))";
        this.insertClan = "INSERT INTO clans(id, clanname, clantag, members) VALUES (?, ?, ?, ?)";
        this.deleteClan = "DELETE FROM clans WHERE id = ?";
        this.selectClanById = "SELECT * FROM clans WHERE id = ?";

        // clans_by_name table
        this.createClansByNameTable = "CREATE TABLE IF NOT EXISTS clans_by_name(name VARCHAR(16), clan VARCHAR(36), PRIMARY KEY(name))";
        this.insertClanName = "INSERT INTO clans_by_name(name, clan) VALUES (?, ?)";
        this.deleteClanName = "DELETE FROM clans_by_name WHERE name = ?";
        this.updateClanName = "UPDATE clans_by_name SET name = ? WHERE name = ?";
        this.selectClanByName = "SELECT clan FROM clans_by_name WHERE name = ?";

        // clans_by_tag table
        this.createClansByTagTable = "CREATE TABLE IF NOT EXISTS clans_by_tag(tag VARCHAR(5), clan VARCHAR(36), PRIMARY KEY(tag))";
        this.insertClanTag = "INSERT INTO clans_by_tag(tag, clan) VALUES (?, ?)";
        this.deleteClanTag = "DELETE FROM clans_by_tag WHERE tag = ?";
        this.updateClanTag = "UPDATE clans_by_tag SET tag = ? WHERE tag = ?";
        this.selectClanByTag = "SELECT clan FROM clans_by_tag WHERE tag = ?";
    }

    public ClanRepositorySQLImplementation createTables() {
        clanSystem.getDatabaseConnector().getMySQL().update(createClansTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createClansByNameTable);
        clanSystem.getDatabaseConnector().getMySQL().update(createClansByTagTable);
        return this;
    }

    @Override
    public ListenableFuture<Clan> insertClan(Clan clan) {
        StringBuilder serializedMembers = new StringBuilder();
        for (Map.Entry<UUID, Clan.Group> entry : clan.getMembers().entrySet()) {
            serializedMembers
                    .append(entry.getKey().toString()).append("=")
                    .append(entry.getValue()).append(";");
        }
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClan,
                clan.getClanId().toString(),
                clan.getClanName(),
                clan.getClanTag(),
                serializedMembers.toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanName,
                clan.getClanName().toLowerCase(),
                clan.getClanId().toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                insertClanTag,
                clan.getClanTag().toLowerCase(),
                clan.getClanId().toString()
        );
        return Futures.immediateFuture(clan);
    }

    @Override
    public ListenableFuture<Void> deleteClan(Clan clan) {
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClan,
                clan.getClanId().toString()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClanName,
                clan.getClanName().toLowerCase()
        );
        clanSystem.getDatabaseConnector().getMySQL().update(
                deleteClanTag,
                clan.getClanTag().toLowerCase()
        );
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<Pair<String, String>> renameClan(Clan clan) {
        return Futures.immediateFuture(new Pair<>(clan.getClanName(), clan.getClanTag()));
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

    private Map<UUID, Clan.Group> parseMembers(String s) {
        String[] args = s.split(";");
        Map<UUID, Clan.Group> members = new HashMap<>();
        Arrays.stream(args)
                .map(member -> member.split("="))
                .forEach(uuidRank -> members.put(UUID.fromString(uuidRank[0]), Clan.Group.valueOf(uuidRank[1])));
        return members;
    }
}
