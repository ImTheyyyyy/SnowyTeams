package dev.snowy.listener;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static dev.snowy.utils.ChatUtil.parse;

public class DeathListener implements Listener {
    private final SnowyTeams plugin;

    public DeathListener(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || !(killer instanceof Player)) {
            return;
        }

        ClanData clanData = plugin.getClanData();
        Clan victimClan = clanData.getClanByPlayer(victim.getName());
        Clan killerClan = clanData.getClanByPlayer(killer.getName());

        int pointsPerKill = plugin.getConfigManager().getConfig().getInt("pointsPerKill", 5);
        int pointsPerDeath = plugin.getConfigManager().getConfig().getInt("pointsPerDeath", 5);

        if (victimClan != null && victimClan.equals(killerClan)) {
            return;
        }

        if (killerClan != null) {
            killerClan.addPoints(pointsPerKill);
            killer.sendMessage(parse(plugin.getPrefix() + plugin.getConfigManager().getConfig().getString("Messages.Kill").replace("%points%", String.valueOf(pointsPerKill).replace("%victim%", victim.getName()))));
        }

        if (victimClan != null) {
            victimClan.removePoints(pointsPerDeath);
            victim.sendMessage(parse(plugin.getPrefix() + plugin.getConfigManager().getConfig().getString("Messages.Death").replace("%poins%", String.valueOf(pointsPerDeath)).replace("%killer%", killer.getName())));
        }
    }
}
