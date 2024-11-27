package dev.snowy.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MoneyManager {
    private static Economy economy = null;

    public static void initializeEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vault plugin no encontrado! Asegúrate de tener Vault instalado.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("No se encontró un sistema de economía compatible con Vault.");
            return;
        }

        economy = rsp.getProvider();
        Bukkit.getLogger().info("Economía de Vault cargada correctamente.");
    }

    public static boolean hasEnoughMoney(OfflinePlayer player, double amount) {
        if (economy == null) {
            Bukkit.getLogger().warning("La economía no está inicializada.");
            return false;
        }
        return economy.getBalance(player) >= amount;
    }

    public static void withdrawMoney(OfflinePlayer player, double amount) {
        if (economy == null) {
            Bukkit.getLogger().warning("La economía no está inicializada.");
            return;
        }
        economy.withdrawPlayer(player, amount);
    }
}
