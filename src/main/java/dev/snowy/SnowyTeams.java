package dev.snowy;

import dev.snowy.commands.ClanAdminCommand;
import dev.snowy.commands.ClanAdminTabCompleter;
import dev.snowy.commands.ClanCommand;
import dev.snowy.config.ConfigManager;
import dev.snowy.listener.AttackListener;
import dev.snowy.listener.ChatListener;
import dev.snowy.listener.DeathListener;
import dev.snowy.manager.ClanData;
import dev.snowy.manager.MoneyManager;
import dev.snowy.manager.StorageManager;
import dev.snowy.placeholderapi.ClanPlaceholderExpansion;
import dev.snowy.utils.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import static dev.snowy.utils.ChatUtil.parse;

public final class SnowyTeams extends JavaPlugin {

    private ClanData clanData;
    private ConfigManager configManager;
    private final Set<String> clanChat = new HashSet<>();
    private static SnowyTeams instance;
    private static String prefix;
    private StorageManager storageManager;
    private Requirements requirements;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        Bukkit.getConsoleSender().sendMessage("SnowyTeams is loading...");
        Bukkit.getConsoleSender().sendMessage("Trying to load modules...");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "-------------------------------------------");

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.requirements = new Requirements(this);

        this.storageManager = new StorageManager(this.configManager.getConfig());
        this.storageManager.initializeStorage();
        Bukkit.getConsoleSender().sendMessage(parse("&7The module &fStorage Type &7has been &aenabled&7!"));

        prefix = configManager.getConfig().getString("prefix");

        this.clanData = new ClanData(storageManager);
        clanData.loadClans();
        Bukkit.getConsoleSender().sendMessage(parse("&7The module &fPoints &7has been &aenabled&7!"));

        MoneyManager.initializeEconomy();
        Bukkit.getConsoleSender().sendMessage(parse("&7The module &fEconomy &7has been &aenabled&7!"));

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholderExpansion(clanData).register();
            Bukkit.getConsoleSender().sendMessage(parse("&7The module &fPlaceholderAPI &7has been &aenabled&7!"));
        }

        registerCommands();
        registerListeners();

        Bukkit.getConsoleSender().sendMessage(parse("&7The module &fEvents &7has been &aenabled&7!"));
        Bukkit.getConsoleSender().sendMessage(parse("&7The module &fCommands &7has been &aenabled&7!"));

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "-------------------------------------------");

        sendStartupMessage();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.configManager.saveConfig();

        if (clanData != null) {
            clanData.saveClans();
        }

        storageManager.closeConnection();
    }

    private void sendStartupMessage() {
        Bukkit.getConsoleSender().sendMessage(parse("&bSnowyTeams has been loaded!"));
        Bukkit.getConsoleSender().sendMessage(parse("&bVersion: &f" + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(parse("&bAuthor: &f" + getDescription().getAuthors()));
        Bukkit.getConsoleSender().sendMessage(parse("&bWebsite: &f" + getDescription().getWebsite()));
        Bukkit.getConsoleSender().sendMessage(parse("&bGet support in the Discord specified in the resource page!"));
    }

    public Requirements getRequirements() {
        return requirements;
    }

    public Set<String> getClanChat() {
        return clanChat;
    }

    public String getPrefix() {
        return prefix;
    }

    public ClanData getClanData() {
        return clanData;
    }

    public static SnowyTeams getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private void registerCommands() {
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("clanadmin").setExecutor(new ClanAdminCommand(this));

        this.getCommand("clan").setTabCompleter(new ClanCommand(this));
        this.getCommand("clanadmin").setTabCompleter(new ClanAdminTabCompleter(this));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new DeathListener(this), this);
        pm.registerEvents(new AttackListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
    }
}
