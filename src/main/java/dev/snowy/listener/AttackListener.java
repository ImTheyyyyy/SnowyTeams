package dev.snowy.listener;

import dev.snowy.SnowyTeams;
import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static dev.snowy.utils.ChatUtil.parse;

public class AttackListener implements Listener {
    private final SnowyTeams plugin;
    private FileConfiguration config = SnowyTeams.getInstance().getConfigManager().getConfig();


    public AttackListener(SnowyTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClanMemberDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();
            ClanData clanData = plugin.getClanData();

            Clan damagerClan = clanData.getClanByPlayer(damager.getName());
            Clan targetClan = clanData.getClanByPlayer(target.getName());

            if (damagerClan != null && damagerClan.equals(targetClan) && !damagerClan.isPvpEnabled()) {
                event.setCancelled(true);
                damager.sendMessage(parse(plugin.getPrefix() + config.getString("Messages.no-members-damage")));
            }
        }
    }
}