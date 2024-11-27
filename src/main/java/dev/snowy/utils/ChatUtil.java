package dev.snowy.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String parse(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, hexToMinecraftColor(hexColor));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private static String hexToMinecraftColor(String hex) {
        StringBuilder minecraftColor = new StringBuilder("§x");
        for (char ch : hex.toCharArray()) {
            minecraftColor.append('§').append(ch);
        }
        return minecraftColor.toString();
    }
}