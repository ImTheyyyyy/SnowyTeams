package dev.snowy.commands;

import dev.snowy.SnowyTeams;
import dev.snowy.config.ConfigManager;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import org.bukkit.block.data.type.Snow;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static dev.snowy.utils.ChatUtil.parse;

public class ClanAdminCommand implements CommandExecutor {
    private final SnowyTeams plugin;
    private FileConfiguration config = SnowyTeams.getInstance().getConfigManager().getConfig();

    public ClanAdminCommand(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.consoleCommand")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.correct-usage")));
            return true;
        }

        if (!player.hasPermission("clan.admin")) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.noPermissions")));
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "score":
                if (args.length < 4) {
                    player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.scorecommand-correct-usage")));
                    return true;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    handleAddScore(player, args);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    handleRemoveScore(player, args);
                } else if (args[1].equalsIgnoreCase("set")) {
                    handleSetScore(player, args);
                } else {
                    player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-correct-usage")));
                }
                break;

            case "forcedisband":
                handleForceDisband(player, args);
                break;

            case "forcekick":
                handleForceKick(player, args);
                break;

            case "forceleader":
                handleForceLeader(player, args);
                break;

            case "forcerename":
                handleForceRename(player, args);
                break;

            case "forcejoin":
                handleForceJoin(player, args);
                break;

            case "reload":
                handleReload(player);
                break;

            default:
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.correct-usage")));
                break;
        }

        return true;
    }

    private void handleReload(Player player) {
        plugin.getConfigManager().reloadConfig();
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.reload-message")));
    }

    private void handleForceJoin(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcejoin-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String clanName = args[2];
        String playerName = args[3];

        Clan clan = clanData.getClanByName(clanName);

        if (clan != null) {
            if (clan.getMembers().contains(playerName)) {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcejoin-player-in-clan").replace("%player%", playerName).replace("%clan%", clanName)));
                return;
            }

            List<String> members = clan.getMembers();
            if (!members.isEmpty()) {
                String randomMember = members.get(new Random().nextInt(members.size()));
                clan.removeMember(randomMember);
            }

            clan.getMembers().add(playerName);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.").replace("%player%", playerName).replace("%clan%", clanName)));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", clanName)));
        }
    }

    private void handleForceRename(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcerename-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String oldClanName = args[2];
        String newClanName = args[3];

        Clan clan = clanData.getClanByName(oldClanName);

        if (clan != null) {
            clan.setName(newClanName);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcerename-renamed").replace("%oldName%", oldClanName).replace("%newName%", newClanName)));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", oldClanName)));
        }
    }

    private void handleForceLeader(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forceleader-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String clanName = args[2];
        String newLeaderName = args[3];

        Clan clan = clanData.getClanByName(clanName);

        if (clan != null) {
            if (clan.getMembers().contains(newLeaderName)) {
                String oldLeaderName = clan.getLeader();
                clan.setLeader(newLeaderName);

                if (oldLeaderName != null && !oldLeaderName.equals(newLeaderName)) {
                    clan.removeMember(oldLeaderName);
                    clan.getMembers().add(oldLeaderName);
                }

                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forceleader-named").replace("%newLeader%", newLeaderName).replace("%clanName%", clanName)));
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forceleader-old-leader").replace("%oldLeader%", oldLeaderName)));
            } else {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forceleader-not-member").replace("%player%", newLeaderName).replace("%clan%", clanName)));
            }
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", clanName)));
        }
    }

    private void handleForceKick(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcekick-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String playerNameToKick = args[2];

        Clan clanOfPlayerToKick = clanData.getClanByPlayer(playerNameToKick);

        if (clanOfPlayerToKick != null) {
            if (clanOfPlayerToKick.getMembers().contains(playerNameToKick)) {
                clanOfPlayerToKick.removeMember(playerNameToKick);
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcekick-kicked").replace("%player%", playerNameToKick).replace("%clan%", clanOfPlayerToKick.getName())));
            } else {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcekick-not-member").replace("%player%", playerNameToKick).replace("%clan%", clanOfPlayerToKick.getName())));
            }
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcekick-no-clan").replace("%player%", playerNameToKick)));
        }
    }

    private void handleForceDisband(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcedisband-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String clanName = args[2];
        Clan clanByName = clanData.getClanByName(clanName);

        if (clanByName != null) {
            clanData.removeClan(clanName);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcedisband-disbanded").replace("%clan%", clanName)));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.forcedisband-not-found").replace("%clan%", clanName)));
        }
    }

    private void handleAddScore(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        Clan clanByName = clanData.getClanByName(args[2]);

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-invalid-amount")));
            return;
        }

        if (clanByName != null) {
            clanByName.addPoints(amount);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-added").replace("%amount%", String.valueOf(amount)).replace("%clan%", clanByName.getName())));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", args[2])));
        }
    }

    private void handleRemoveScore(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-remove-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String clanName = args[2];
        Clan clanByName = clanData.getClanByName(clanName);

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-invalid-amount")));
            return;
        }

        if (clanByName != null) {
            if (clanByName.getPoints() <= 0) {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-not-negative")));
                return;
            }

            int currentPoints = clanByName.getPoints();
            int newPoints = currentPoints - amount;

            if (newPoints < 0) {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-below-zero").replace("%currentPoints%", String.valueOf(currentPoints))));
                return;
            }

            clanByName.removePoints(amount);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-removed").replace("%amount%", String.valueOf(amount)).replace("%clan%", clanByName.getName())));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", clanName)));
        }
    }

    private void handleSetScore(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-set-correct-usage")));
            return;
        }

        ClanData clanData = plugin.getClanData();
        String clanName = args[2];
        Clan clanByName = clanData.getClanByName(clanName);

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-invalid-amount")));
            return;
        }

        if (clanByName != null) {
            clanByName.setPoints(amount);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.score-set").replace("%amount%", String.valueOf(amount)).replace("%clan%", clanByName.getName())));
        } else {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanAdminCommand.clan-not-found").replace("%args%", clanName)));
        }
    }
}