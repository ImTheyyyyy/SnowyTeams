package dev.snowy.commands;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import dev.snowy.utils.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.snowy.utils.ChatUtil.parse;

public class ClanCommand implements CommandExecutor, Listener, TabCompleter {
    private final SnowyTeams plugin;
    private Map<String, Long> createCooldowns = new HashMap<>();
    private FileConfiguration config = SnowyTeams.getInstance().getConfigManager().getConfig();

    public ClanCommand(SnowyTeams plugin) {
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
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.correct-usage")));
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "create":
                handleCreate(player, args);
                break;

            case "disband":
                handleDisband(player);
                break;

            case "invite":
                handleInvite(player, args);
                break;

            case "join":
                handleJoin(player, args);
                break;

            case "kick":
                handleKick(player, args);
                break;

            case "leave":
                handleLeave(player);
                break;

            case "rename":
                handleRename(player, args);
                break;

            case "leader":
                handleLeader(player, args);
                break;

            case "chat":
                handleChat(player);
                break;

            case "pvp":
                handlePvP(player);
                break;

            case "info":
                handleInfo(player, args);
                break;

            case "top":
                handleTop(player);
                break;

            case "help":
                handleHelp(player);
                break;

            case "description":
                handleDescription(player, args);
                break;

            default:
                handleHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.correct-usage")));
            return;
        }

        if (clanData.getClanByLeader(player.getName()) != null || clanData.getClanByMember(player.getName()) != null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.already-in-clan")));
            return;
        }

        String clanName = args[1].toLowerCase();
        int minTeamLength = plugin.getConfigManager().getConfig().getInt("minTeamLength", 3);
        int maxTeamLength = plugin.getConfigManager().getConfig().getInt("maxTeamLength", 12);

        if (clanName.length() < minTeamLength) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.name-too-short").replace("%minTeamLength%", String.valueOf(minTeamLength))));
            return;
        }

        if (clanName.length() > maxTeamLength) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.name-too-long").replace("%maxTeamLength%", String.valueOf(maxTeamLength))));
            return;
        }

        if (clanData.getClan(clanName) != null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.name-taken")));
            return;
        }

        List<String> blockedNames = plugin.getConfig().getStringList("BlockedNames");
        if (blockedNames.contains(clanName.toLowerCase())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.blocked-name")));
            return;
        }

        long currentTime = System.currentTimeMillis();
        long cooldown = plugin.getConfigManager().getConfig().getInt("createCooldown", 1000);
        long cooldownTime = cooldown * 1000;
        if (createCooldowns.containsKey(player.getName())) {
            long lastCreateTime = createCooldowns.get(player.getName());
            if (currentTime - lastCreateTime < cooldownTime) {
                long timeLeft = (cooldownTime - (currentTime - lastCreateTime)) / 1000;
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.cooldown").replace("%time%", String.valueOf(timeLeft))));
                return;
            }
        }

        Requirements requirements = plugin.getRequirements();
        if (requirements.processRequirement(player, "create")) {
            Clan newClan = new Clan(clanName, player.getName(), new ArrayList<>());
            clanData.addClan(clanName, newClan);

            createCooldowns.put(player.getName(), currentTime);
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.create.success").replace("%clan%", clanName)));
        }
    }

    private void handleDisband(Player player) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());
        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.no-leader")));
            return;
        }

        clanData.removeClan(clan.getName());
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.disband-dissolved").replace("%clan%", clan.getName())));
    }

    private void handleInvite(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.no-leader")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.correct-usage")));
            return;
        }

        if (clan.getMembers().size() + 1 >= plugin.getConfigManager().getConfig().getInt("maxTeamMembers", 5)) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.max-members")));
            return;
        }

        String targetName = args[1];
        Player targetPlayer = plugin.getServer().getPlayer(targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.player-not-online")));
            return;
        }

        if (targetName.equals(player.getName())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.self-invite")));
            return;
        }

        if (clanData.getClanByMember(targetPlayer.getName()) != null || clanData.getClanByLeader(targetPlayer.getName()) != null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.already-in-clan")));
            return;
        }

        clanData.addInvitation(targetName, clan.getName());
        targetPlayer.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.invitation-received").replace("%clan%", clan.getName())));
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.invitation-sent").replace("%target%", targetName)));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            clanData.removeInvitation(targetName, clan.getName());
            targetPlayer.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.invite.invitation-expired").replace("%clan%", clan.getName())));
        }, 1200L);
    }

    private void handleJoin(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.join.correct-usage")));
            return;
        }

        if (clanData.getClanByMember(player.getName()) != null || clanData.getClanByLeader(player.getName()) != null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.join.already-in-clan")));
            return;
        }

        String clanName = args[1].toLowerCase();
        if (!clanData.isInvited(player.getName(), clanName)) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.join.no-invitation")));
            return;
        }

        clanData.addMember(clanName, player.getName());
        clanData.removeInvitation(player.getName(), clanName);

        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.join.success").replace("%clan%", clanName)));

        Clan clan = clanData.getClanByName(clanName);
        if (clan != null) {
            for (String memberName : clan.getMembers()) {
                Player member = Bukkit.getPlayer(memberName);
                if (member != null && member.isOnline()) {
                    String memberJoinedMessage = config.getString("Messages.clanCommand.join.member-joined", "Default message if not set.")
                            .replace("%player%", player.getName())
                            .replace("%clan%", clanName);
                    member.sendMessage(parse(plugin.getPrefix() + memberJoinedMessage));
                }
            }
        }

        String leaderName = clan.getLeader();
        Player leader = Bukkit.getPlayer(leaderName);
        if (leader != null && leader.isOnline()) {
            String leaderNotificationMessage = config.getString("Messages.clanCommand.join.member-joined", "Default message if not set.")
                    .replace("%player%", player.getName())
                    .replace("%clan%", clanName);
            leader.sendMessage(parse(plugin.getPrefix() + leaderNotificationMessage));
        }
    }

    private void handleKick(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.not-leader")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.correct-usage")));
            return;
        }

        String targetName = args[1];
        if (targetName.equals(player.getName())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.self-kick")));
            return;
        }

        if (!clan.getMembers().contains(targetName)) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.not-member").replace("%player%", targetName)));
            return;
        }

        Clan targetClan = clanData.getClanByMember(targetName);
        if (targetClan == null || !targetClan.getName().equals(clan.getName())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.not-in-clan").replace("%player%", targetName)));
            return;
        }

        clanData.kickMember(clan.getName(), targetName);
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.success").replace("%player%", targetName).replace("%clan%", clan.getName())));

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.kick.expelled").replace("%clan%", clan.getName())));
        }
    }

    private void handleLeave(Player player) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByMember(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leave.not-member")));
            return;
        }

        if (clan.getLeader().equals(player.getName())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leave.leader-leave")));
            return;
        }

        clanData.removeMember(clan.getName(), player.getName());
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leave.success").replace("%clan%", clan.getName())));
    }

    private void handleDescription(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByPlayer(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.description.not-clan", "Default Message")));
            return;
        }

        if (!clan.getLeader().equals(player.getName())) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.description.not-leader", "Default Message")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.description.usage", "Default Message")));
            return;
        }

        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        clan.setDescription(description);

        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.description.success", "Default Message").replace("%description%", description)));
    }

    private void handleRename(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.rename.not-leader", "Default Message")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.rename.usage", "Default Message")));
            return;
        }

        String newClanName = args[1].toLowerCase();
        if (newClanName.isEmpty()) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.rename.empty-name", "Default Message")));
            return;
        }

        if (clanData.getClan(newClanName) != null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.rename.taken-name", "Default Message").replace("%newName%", newClanName)));
            return;
        }

        clanData.renameClan(clan.getName(), newClanName);
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.rename.success", "Default Message").replace("%newName%", newClanName)));
    }

    private void handleLeader(Player player, String[] args) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leader.not-leader")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leader.usage")));
            return;
        }

        String newLeaderName = args[1];
        if (!clan.getMembers().contains(newLeaderName)) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leader.not-member").replace("%newLeader%", newLeaderName)));
            return;
        }

        clanData.setLeader(clan.getName(), newLeaderName);
        player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leader.success").replace("%newLeader%", newLeaderName)));

        clan.getMembers().remove(player.getName()); // Remover al antiguo líder de los miembros
        clan.getMembers().add(newLeaderName); // Asegurarse de que el nuevo líder está en la lista

        Player newLeaderPlayer = Bukkit.getPlayer(newLeaderName);
        if (newLeaderPlayer != null && newLeaderPlayer.isOnline()) {
            newLeaderPlayer.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.leader.new-leader").replace("%clanName%", clan.getName())));
        }
    }

    private void handleChat(Player player) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByPlayer(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.chat.not-member")));
            return;
        }

        Set<String> clanChat = plugin.getClanChat();

        if (clanChat.contains(player.getName())) {
            clanChat.remove(player.getName());
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.chat.left")));
        } else {
            clanChat.add(player.getName());
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.chat.joined")));
        }
    }

    private void handleHelp(Player player) {
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan create [name] &f¦ &bCreate a Clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan disband &f¦ &bDisband your clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan invite [player] &f¦ &bInvite a player"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan join [name] &f¦ &bJoin a clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan kick [player] &f¦ &bKick a player"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan leave &f¦ &bLeave the clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan description [description] &f¦ &bGive your clan a description"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan rename [name] &f¦ &bRename your clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan leader [player] &f¦ &bGive a player a leader"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan chat &f¦ &bToggle clan chat"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan pvp &f¦ &bToggle clan pvp"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan info [name] &f¦ &bDisplays info about a clan"));
        player.sendMessage(parse(plugin.getPrefix() + "&7/clan top &f¦ &bShows the top clans"));
    }

    private void handlePvP(Player player) {
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByLeader(player.getName());

        if (clan == null) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.pvp.not-leader")));
            return;
        }

        clan.setPvpEnabled(!clan.isPvpEnabled());
        String pvpStatus = clan.isPvpEnabled() ? config.getString("Messages.clanCommand.pvp.enabled") : config.getString("Messages.clanCommand.pvp.disabled");
        player.sendMessage(parse(plugin.getPrefix() + pvpStatus));
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.info.usage")));
            return;
        }

        String targetName = args[1];
        ClanData clanData = plugin.getClanData();
        Clan clanByName = clanData.getClanByName(targetName);

        if (clanByName != null) {
            sendClanInfo(player, clanByName);
        } else {
            Clan clan = clanData.getClanByPlayer(targetName);
            if (clan != null) {
                sendClanInfo(player, clan);
            } else {
                player.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.clanCommand.info.notFound")));
            }
        }
    }

    private void sendClanInfo(Player player, Clan clan) {
        List<String> messages = config.getStringList("Messages.clanCommand.info.clanInfo");
        for (String message : messages) {
            String formattedMessage = message
                    .replace("%clanName%", clan.getName())
                    .replace("%leader%", clan.getLeader())
                    .replace("%points%", String.valueOf(clan.getPoints()))
                    .replace("%members%", String.join(", ", clan.getMembers()))
                    .replace("%description%", clan.getDescription());
            player.sendMessage(parse(plugin.getPrefix() + formattedMessage));
        }
    }

    private void handleTop(Player player) {
        ClanData clanData = plugin.getClanData();
        List<Clan> clans = clanData.getAllClans();

        if (clans.isEmpty()) {
            player.sendMessage(parse(config.getString("Messages.clanCommand.top.noClans")));
            return;
        }

        clans.sort(Comparator.comparingInt(Clan::getPoints).reversed());

        player.sendMessage(parse(config.getString("Messages.clanCommand.top.title")));

        for (int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            String formattedEntry = config.getString("Messages.clanCommand.top.entry")
                    .replace("%rank%", String.valueOf(i + 1))
                    .replace("%clanName%", clan.getName())
                    .replace("%points%", String.valueOf(clan.getPoints()));

            player.sendMessage(parse(formattedEntry));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] subcommands = { "create", "description", "disband", "invite", "join", "kick", "leave", "rename", "leader", "chat", "pvp", "info", "top" };
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }

        // Completions para el subcomando 'invite'
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            }

            // Completions para el subcomando 'join'
            else if (args[0].equalsIgnoreCase("join")) {
                ClanData clanData = plugin.getClanData();
                for (Clan clan : clanData.getAllClans()) { // Assuming getAllClans() returns a collection of Clan objects
                    if (clan.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(clan.getName()); // Add the clan name to completions
                    }
                }
            }
        }
        return completions;
    }
}