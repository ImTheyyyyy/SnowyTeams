package dev.snowy.utils;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.MoneyManager;
import org.bukkit.entity.Player;

import static dev.snowy.utils.ChatUtil.parse;

public class Requirements {
    private final SnowyTeams plugin;

    public Requirements(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    public boolean processRequirement(Player player, String requirement) {
        String type = plugin.getConfigManager().getConfig().getString("Requirements." + requirement + ".type");
        int amount = plugin.getConfigManager().getConfig().getInt("Requirements." + requirement + ".price");

        if (requirement == null) {
            plugin.getLogger().info("The requirement is not valid.");
            return true;
        }

        if (type == null) {
            plugin.getLogger().info("The type is not valid.");
            return true;
        }

        switch (type) {
            case "MONEY":
                if (!plugin.getConfigManager().getConfig().getBoolean("Requirements" + requirement + ".enabled")) {
                    return true;
                }

                if (!MoneyManager.hasEnoughMoney(player, amount)) {
                    player.sendMessage(parse(plugin.getPrefix() + plugin.getConfigManager().getConfig().getString("Messages.clanCommand.create.requirement", "&cYou need $%amount% of money to create a clan.").replace("%amount%", String.valueOf(amount))));
                    return true;
                }

                MoneyManager.withdrawMoney(player, amount);
                break;

            case "LEVEL":
                if (!plugin.getConfigManager().getConfig().getBoolean("Requirements" + requirement + ".enabled")) {
                    return true;
                }

                if (player.getLevel() < amount) {
                    player.sendMessage(parse(plugin.getPrefix() + plugin.getConfigManager().getConfig().getString("Messages.clanCommand.create.requirement", "&cYou need %amount% levels to create a clan.").replace("%amount%", String.valueOf(amount))));
                    return true;
                }

                if (player.getLevel() >= amount) {
                    player.setLevel(player.getLevel() - amount);
                    return true;
                }

                break;
        }

        return  true;
    }
}