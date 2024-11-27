package dev.snowy.commands;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClanAdminTabCompleter implements TabCompleter {
    private final SnowyTeams plugin;

    public ClanAdminTabCompleter(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Check if the sender is a player and has the required permission
        if (!(sender instanceof Player) || !sender.hasPermission("clan.admin")) {
            return completions; // No completions if not a player or lacks permission
        }

        if (args.length == 1) {
            // Suggest subcommands
            completions.add("score");
            completions.add("forcedisband");
            completions.add("forcekick");
            completions.add("forceleader");
            completions.add("forcerename");
            completions.add("forcejoin");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("score")) {
                completions.add("add");
                completions.add("remove");
                completions.add("set");
            }

            // Suggest clan names for specific commands
            if (args[0].equalsIgnoreCase("forcedisband") ||
                    args[0].equalsIgnoreCase("forcekick") || args[0].equalsIgnoreCase("forceleader") ||
                    args[0].equalsIgnoreCase("forcerename") || args[0].equalsIgnoreCase("forcejoin")) {

                // Fetch clan names from ClanData
                ClanData clanData = plugin.getClanData();
                for (Clan clan : clanData.getAllClans()) {
                    completions.add(clan.getName());
                }
            }

            // You can add more logic here for other commands' arguments if needed
        } else if (args.length == 3 && args[0].equalsIgnoreCase("score")) {
            for (Clan clan : plugin.getClanData().getAllClans()) {
                completions.add(clan.getName());
            }
        }

        // Return filtered completions
        return completions;
    }
}