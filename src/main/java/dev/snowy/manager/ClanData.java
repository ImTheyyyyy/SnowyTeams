package dev.snowy.manager;

import dev.snowy.SnowyTeams;
import dev.snowy.teams.Clan;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.snowy.utils.ChatUtil.parse;

public class ClanData {
    private final StorageManager storageManager;
    private final String storageType;
    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<String, String> invitations = new HashMap<>();
    private final File file = new File(SnowyTeams.getInstance().getDataFolder(), "teams.yml");
    private FileConfiguration config;
    private int maxMembers;

    public ClanData(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.storageType = storageManager.getStorageType();

        if ("YAML".equals(storageType)) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = null;
        }

        loadClans();
        maxMembers = SnowyTeams.getInstance().getConfigManager().getConfig().getInt("maxTeamMembers", 5);
    }

    public void loadClans() {
        if ("YAML".equals(storageType)) {
            loadClansFromYAML();
        } else if ("MYSQL".equals(storageType)) {
            loadClansFromMySQL();
        }
    }

    private void loadClansFromYAML() {
        clans.clear();
        for (String key : config.getKeys(false)) {
            String name = config.getString(key + ".name");
            String description = config.getString(key + ".description", "");
            String leader = config.getString(key + ".leader");
            List<String> members = config.getStringList(key + ".members");
            int points = config.getInt(key + ".points", 0);

            Clan clan = new Clan(name, leader, members);
            clan.setPoints(points);
            clan.setDescription(description);
            clans.put(name.toLowerCase(), clan);
        }
    }

    private void createClansTableIfNotExists(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS clans ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(255) NOT NULL, "
                + "description TEXT NOT NULL, "
                + "leader VARCHAR(255) NOT NULL, "
                + "members TEXT, "
                + "points INT DEFAULT 0, "
                + "UNIQUE (name))";

            stmt.executeUpdate(createTableQuery);
        }
    }

    private void loadClansFromMySQL() {
        try (Connection connection = storageManager.getDatabaseConnection()) {
            createClansTableIfNotExists(connection);

            String query = "SELECT * FROM clans";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                clans.clear();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String leader = rs.getString("leader");
                    List<String> members = getMembersFromDB(rs.getString("members"));
                    int points = rs.getInt("points");

                    Clan clan = new Clan(name, leader, members);
                    clan.setPoints(points);
                    clan.setDescription(description);
                    clans.put(name.toLowerCase(), clan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> getMembersFromDB(String membersData) {
        List<String> members = new ArrayList<>();
        if (membersData != null && !membersData.isEmpty()) {
            String[] membersArray = membersData.split(",");
            for (String member : membersArray) {
                members.add(member.trim());
            }
        }
        return members;
    }

    public void saveClans() {
        if ("YAML".equals(storageType)) {
            saveClansToYAML();
        } else if ("MYSQL".equals(storageType)) {
            saveClansToMySQL();
        }
    }

    private void saveClansToYAML() {
        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            String key = entry.getKey();
            Clan clan = entry.getValue();
            config.set(key + ".name", clan.getName());
            config.set(key + ".description", clan.getDescription());
            config.set(key + ".leader", clan.getLeader());
            config.set(key + ".members", clan.getMembers());
            config.set(key + ".points", clan.getPoints());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveClansToMySQL() {
        try (Connection connection = storageManager.getDatabaseConnection()) {
            String query = "INSERT INTO clans (name, description, leader, members, points) " +
                    "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE description = ?, leader = ?, members = ?, points = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                for (Map.Entry<String, Clan> entry : clans.entrySet()) {
                    Clan clan = entry.getValue();

                    stmt.setString(1, clan.getName());
                    stmt.setString(2, clan.getDescription());
                    stmt.setString(3, clan.getLeader());
                    stmt.setString(4, String.join(",", clan.getMembers()));
                    stmt.setInt(5, clan.getPoints());

                    stmt.setString(6, clan.getDescription());
                    stmt.setString(7, clan.getLeader());
                    stmt.setString(8, String.join(",", clan.getMembers()));
                    stmt.setInt(9, clan.getPoints());

                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public void addClan(String name, Clan clan) {
        clans.put(name.toLowerCase(), clan);
        saveClans();
    }

    public void removeClan(String name) {
        clans.remove(name.toLowerCase());
        config.set(name.toLowerCase(), null);
        saveClans();
    }

    public void reloadClans() {
        config = YamlConfiguration.loadConfiguration(file);
        loadClans();
    }

    public Clan getClanByName(String clanName) {
        return clans.get(clanName.toLowerCase());
    }

    public List<Clan> getAllClans() {
        return new ArrayList<>(clans.values());
    }

    public void setLeader(String clanName, String newLeader) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            clan.setLeader(newLeader);
        }
    }

    public Clan getClanByPlayer(String playerName) {
        for (Clan clan : clans.values()) {
            if (clan.getLeader().equalsIgnoreCase(playerName) || clan.getMembers().contains(playerName)) {
                return clan;
            }
        }
        return null;
    }

    public Clan getClanByLeader(String leaderName) {
        for (Clan clan : clans.values()) {
            if (clan.getLeader().equalsIgnoreCase(leaderName)) {
                return clan;
            }
        }
        return null;
    }

    public Clan getClanByMember(String playerName) {
        for (Clan clan : clans.values()) {
            if (clan.getMembers().contains(playerName)) {
                return clan;
            }
        }
        return null;
    }

    public void addMember(String clanName, String playerName) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            int totalMembers = clan.getMembers().size() + 1;
            if (totalMembers < maxMembers) {
                clan.getMembers().add(playerName);
                saveClans();
            } else {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.sendMessage(parse(SnowyTeams.getInstance().getPrefix() + SnowyTeams.getInstance().getConfigManager().getConfig().getString("Messages.MaxMembers").replace("%maxMembers%", String.valueOf(maxMembers))));
                }
            }
        }
    }

    public void renameClan(String oldName, String newName) {
        Clan clan = getClan(oldName);
        if (clan != null) {
            config.set(oldName.toLowerCase(), null);

            clan.setName(newName);
            clans.remove(oldName.toLowerCase());
            clans.put(newName.toLowerCase(), clan);

            saveClans();
        }
    }

    public void removeMember(String clanName, String playerName) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            clan.removeMember(playerName);
        }
    }

    public void kickMember(String clanName, String playerName) {
        Clan clan = getClan(clanName);
        if (clan != null && clan.getMembers().contains(playerName)) {
            clan.getMembers().remove(playerName);
            saveClans();
        }
    }

    public Map<String, Clan> getClans() {
        return clans;
    }

    public void addInvitation(String playerName, String clanName) {
        invitations.put(playerName.toLowerCase(), clanName);
    }

    public void removeInvitation(String playerName, String clanName) {
        invitations.remove(playerName.toLowerCase(), clanName);
    }

    public boolean isInvited(String playerName, String clanName) {
        return clanName.equalsIgnoreCase(invitations.get(playerName.toLowerCase()));
    }
}