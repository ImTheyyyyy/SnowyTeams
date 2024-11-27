package dev.snowy.teams;

import java.util.ArrayList;
import java.util.List;

public class Clan {
    private String name;
    private String leader;
    private String description;
    private List<String> members;
    private int points;
    private boolean pvpEnabled;

    public Clan(String name, String leader, List<String> members) {
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.points = 0;
        this.description = "";
        this.pvpEnabled = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void removePoints(int amount) {
        if (this.points - amount < 0) {
            this.points = 0;
        } else {
            this.points -= amount;
        }
    }

    public List<String> getAllMembers() {
        List<String> allMembers = new ArrayList<>(members);
        allMembers.add(leader);
        return allMembers;
    }

    public void setPoints(int points) {
        this.points = Math.max(points, 0);
    }

    public int getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeader() {
        return leader;
    }

    public void removeMember(String playerName) {
        members.remove(playerName);
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}