package io.github.liam0999.civmod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;

public final class ChatFormat {
    public static final String MOD_LABEL = "CivMod";
    public static final String RADAR_LABEL = "CombatRadar";

    private static final ChatFormatting MOD_COLOR = ChatFormatting.GOLD;
    private static final ChatFormatting RADAR_COLOR = ChatFormatting.DARK_AQUA;

    private static final String SEPARATOR = " » ";

    private ChatFormat() {
    }

    public static MutableComponent prefix() {
        return prefix(MOD_LABEL, MOD_COLOR);
    }

    public static MutableComponent prefixed(Component message) {
        return prefixed(MOD_LABEL, MOD_COLOR, message);
    }

    public static MutableComponent prefixed(String message) {
        return prefixed(Component.literal(message));
    }

    public static MutableComponent radarPrefix() {
        return prefix(RADAR_LABEL, RADAR_COLOR);
    }

    public static MutableComponent radarPrefixed(Component message) {
        return prefixed(RADAR_LABEL, RADAR_COLOR, message);
    }

    public static boolean isRadarMessage(Component message) {
        return message != null && message.getString().startsWith(RADAR_LABEL + SEPARATOR);
    }

    private static MutableComponent prefix(String label, ChatFormatting color) {
        return Component.literal(label).withStyle(color)
            .append(Component.literal(SEPARATOR).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static MutableComponent prefixed(String label, ChatFormatting color, Component message) {
        return prefix(label, color).append(Component.empty().withStyle(ChatFormatting.WHITE).append(message));
    }

    public static MutableComponent link(String url) {
        return Component.literal(url).withStyle(style -> style
            .withColor(ChatFormatting.AQUA)
            .withBold(true)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
    }
}
