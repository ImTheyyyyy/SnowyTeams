package dev.snowy.listener;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

import static dev.snowy.utils.ChatUtil.parse;

public class ChatListener implements Listener {
    private final SnowyTeams plugin;

    public ChatListener(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ClanData clanData = plugin.getClanData();
        Clan clan = clanData.getClanByPlayer(player.getName());

        if (clan == null) {
            return;
        }

        Set<String> clanChat = plugin.getClanChat();

        if (clanChat.contains(player.getName())) {
            event.setCancelled(true);

            String format = plugin.getConfigManager().getConfig().getString("Messages.clanCommand.chat.format", "&7[%clan_name%] &f%player_name%: &b%message%");

            String messageToSend = parse(format
                    .replace("%clan_name%", clan.getName())
                    .replace("%player_name%", player.getName())
                    .replace("%message%", event.getMessage()));

            for (String member : clan.getAllMembers()) {
                Player clanMember = Bukkit.getPlayer(member);
                if (clanMember != null && clanMember.isOnline()) {
                    clanMember.sendMessage(messageToSend);
                }
            }
        }
    }
}