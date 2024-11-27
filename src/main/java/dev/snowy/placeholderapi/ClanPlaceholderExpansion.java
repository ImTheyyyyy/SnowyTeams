package dev.snowy.placeholderapi;

import dev.snowy.manager.ClanData;
import dev.snowy.teams.Clan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClanPlaceholderExpansion extends PlaceholderExpansion {
    private final ClanData clanData;

    public ClanPlaceholderExpansion(ClanData clanData) {
        this.clanData = clanData;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "snowyteams";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SnowyEstudios";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Clan clan = clanData.getClanByPlayer(player.getName());
        if (clan == null) {
            return "";
        }

        if (params.equalsIgnoreCase("name")) {
            return clan.getName();
        }

        if (params.equalsIgnoreCase("points")) {
            return String.valueOf(clan.getPoints());
        }

        if (params.equalsIgnoreCase("leader")) {
            return clan.getLeader();
        }

        if (params.startsWith("points_")) {
            String clanName = params.split("_")[1];
            Clan targetClan = clanData.getClanByName(clanName);

            if (targetClan != null) {
                return String.valueOf(targetClan.getPoints());
            } else {
                return "null";
            }
        }

        if (params.startsWith("position_score_")) {
            try {
                int position = Integer.parseInt(params.split("_")[2]) - 1;

                List<Clan> sortedClans = clanData.getAllClans().stream()
                        .sorted(Comparator.comparingInt(Clan::getPoints).reversed())
                        .collect(Collectors.toList());

                if (position >= 0 && position < sortedClans.size()) {
                    return sortedClans.get(position).getName();
                } else {
                    return "null";
                }

            } catch (NumberFormatException e) {
                return "null";
            }
        }

        return null;
    }
}
