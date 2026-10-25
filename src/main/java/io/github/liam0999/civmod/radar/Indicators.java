package io.github.liam0999.civmod.radar;

import net.minecraft.ChatFormatting;

public class Indicators {
    public static final int NEAR_DISTANCE = 30;
    public static final int MID_DISTANCE = 75;

    public static final String WARNING = "⚠";
    private static final String ARROW_UP = "↑";
    private static final String ARROW_DOWN = "↓";

    private static final double Y_THRESHOLD = 1.0;

    public static ChatFormatting distanceColor(double distance) {
        if (distance < NEAR_DISTANCE) return ChatFormatting.RED;
        if (distance < MID_DISTANCE) return ChatFormatting.YELLOW;
        return ChatFormatting.GREEN;
    }

    public static String verticalArrow(double deltaY) {
        if (deltaY >= Y_THRESHOLD) return ARROW_UP;
        if (deltaY <= -Y_THRESHOLD) return ARROW_DOWN;
        return "";
    }
}
