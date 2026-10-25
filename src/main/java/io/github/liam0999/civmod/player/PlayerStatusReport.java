package io.github.liam0999.civmod.player;

import io.github.liam0999.civmod.util.ChatFormat;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class PlayerStatusReport {
    private static final Pattern SPECIAL_CODES = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private final PlayerSettings _config;

    public PlayerStatusReport(PlayerSettings config) {
        _config = config;
    }

    public void report(boolean includeAllies, boolean includeEnemies) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getConnection() == null || minecraft.player == null) {
            log(Component.literal("You are not connected to a server").withStyle(ChatFormatting.RED));
            return;
        }

        List<String> allies = new ArrayList<>();
        List<String> enemies = new ArrayList<>();

        Collection<PlayerInfo> players = minecraft.getConnection().getOnlinePlayers();
        for (PlayerInfo info : players) {
            GameProfile profile = info.getProfile();

            if (profile.id().equals(minecraft.player.getUUID()))
                continue;

            String name = SPECIAL_CODES.matcher(profile.name()).replaceAll("");
            PlayerType type = _config.getPlayerType(name);

            if (type == PlayerType.Ally) {
                allies.add(name);
            } else if (type == PlayerType.Enemy) {
                enemies.add(name);
            }
        }

        allies.sort(String.CASE_INSENSITIVE_ORDER);
        enemies.sort(String.CASE_INSENSITIVE_ORDER);

        if (includeAllies)
            log(line("Allies", allies, PlayerType.Ally));

        if (includeEnemies)
            log(line("Enemies", enemies, PlayerType.Enemy));
    }

    private MutableComponent line(String label, List<String> names, PlayerType type) {
        int color = _config.getPlayerTypeInfo(type).color.getRGB();

        MutableComponent line = Component.literal(label + ": ").withStyle(ChatFormatting.GRAY);

        if (names.isEmpty()) {
            return line.append(Component.literal("none online").withStyle(ChatFormatting.DARK_GRAY));
        }

        line.append(Component.literal(names.size() + " online").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY));

        for (int i = 0; i < names.size(); i++) {
            if (i > 0)
                line.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));

            line.append(Component.literal(names.get(i)).withStyle(style -> style.withColor(color)));
        }

        return line;
    }

    private static void log(Component message) {
        Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(ChatFormat.prefixed(message));
    }
}
