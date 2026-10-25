package io.github.liam0999.civmod.radar;

import io.github.liam0999.civmod.player.PlayerType;

import io.github.liam0999.civmod.player.PlayerSettings;
import io.github.liam0999.civmod.radar.entity.PlayerRadarEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerListOverlay {
    private static final int LINE_SPACING = 2;
    private static final float CENTRE_DEADZONE = 0.05f;

    public enum Align { LEFT, CENTRE, RIGHT }

    // the renderer and the position editor must agree, so both ask these
    public static Align alignFor(float x) {
        if (Math.abs(x - 0.5f) <= CENTRE_DEADZONE) {
            return Align.CENTRE;
        }
        return x < 0.5f ? Align.LEFT : Align.RIGHT;
    }

    public static boolean growsUp(float y) {
        return y > 0.5f;
    }

    public static int alignedLeft(Align align, int anchorX, int width) {
        return switch (align) {
            case LEFT -> anchorX;
            case RIGHT -> anchorX - width;
            case CENTRE -> anchorX - width / 2;
        };
    }

    private final RadarSettings _config;
    private final PlayerSettings _players;

    private boolean _drewThisFrame;
    private int _lastX;
    private int _lastY;
    private int _lastWidth;
    private int _lastHeight;

    public PlayerListOverlay(RadarSettings config, PlayerSettings players) {
        _players = players;
        _config = config;
    }

    public void render(GuiGraphicsExtractor guiGraphics, List<PlayerRadarEntity> players) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        List<MutableComponent> lines = buildLines(minecraft, players);
        if (lines.isEmpty()) return;

        draw(guiGraphics, lines);
    }

    // the real list only draws when players are around, so the position editor needs a stand-in
    public void renderSample(GuiGraphicsExtractor guiGraphics) {
        List<MutableComponent> lines = new ArrayList<>();
        lines.add(formatLine("Enemy", PlayerType.Enemy, 18, "North West", 4));
        lines.add(formatLine("Neutral", PlayerType.Neutral, 52, "East", 0));
        lines.add(formatLine("Ally", PlayerType.Ally, 140, "South", -7));
        draw(guiGraphics, lines);
    }

    private MutableComponent formatLine(String name, PlayerType type, double distance, String direction, double deltaY) {
        ChatFormatting distanceColor = Indicators.distanceColor(distance);

        MutableComponent line = Component.literal(Indicators.WARNING + " ")
                .withStyle(distanceColor)
                .append(Component.literal(name)
                        .withStyle(style -> style.withColor(_players.getPlayerTypeInfo(type).color.getRGB())))
                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(direction).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" (" + Math.round(distance) + "m)").withStyle(distanceColor));

        if (_config.getShowYLevel()) {
            String arrow = Indicators.verticalArrow(deltaY);
            if (!arrow.isEmpty()) {
                line.append(Component.literal(" " + arrow).withStyle(ChatFormatting.GRAY));
            }
        }

        return line;
    }

    public void resetDrawn() {
        _drewThisFrame = false;
    }

    public boolean drewThisFrame() {
        return _drewThisFrame;
    }

    private void draw(GuiGraphicsExtractor guiGraphics, List<MutableComponent> lines) {
        _drewThisFrame = true;

        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + LINE_SPACING;
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        float fx = _config.getPlayerListX();
        float fy = _config.getPlayerListY();
        Align align = alignFor(fx);

        int anchorX = Math.round(fx * screenWidth);
        int anchorY = Math.round(fy * screenHeight);
        int totalHeight = lines.size() * lineHeight;
        int top = growsUp(fy) ? anchorY - totalHeight : anchorY;

        int widest = 0;
        for (MutableComponent line : lines) {
            widest = Math.max(widest, font.width(line));
        }

        for (int i = 0; i < lines.size(); i++) {
            MutableComponent line = lines.get(i);
            int width = font.width(line);
            guiGraphics.text(font, line, alignedLeft(align, anchorX, width), top + i * lineHeight, -1);
        }

        _lastWidth = widest;
        _lastHeight = totalHeight;
        _lastX = alignedLeft(align, anchorX, widest);
        _lastY = top;
    }

    public int getLastX() {
        return _lastX;
    }

    public int getLastY() {
        return _lastY;
    }

    public int getLastWidth() {
        return _lastWidth;
    }

    public int getLastHeight() {
        return _lastHeight;
    }

    private List<MutableComponent> buildLines(Minecraft minecraft, List<PlayerRadarEntity> players) {
        List<PlayerEntry> entries = new ArrayList<>();

        for (PlayerRadarEntity radarEntity : players) {
            if (!_players.getPlayerTypeInfo(radarEntity.getPlayerType()).inPlayerList) continue;

            Entity entity = radarEntity.getEntity();
            double dx = entity.getX() - minecraft.player.getX();
            double dz = entity.getZ() - minecraft.player.getZ();
            double dy = entity.getY() - minecraft.player.getY();

            entries.add(new PlayerEntry(
                    entity.getName().getString(),
                    radarEntity.getPlayerType(),
                    Math.sqrt(dx * dx + dz * dz),
                    direction(dx, dz),
                    dy));
        }

        entries.sort(Comparator.comparingDouble(PlayerEntry::distance));

        List<MutableComponent> lines = new ArrayList<>(entries.size());
        for (PlayerEntry entry : entries) {
            lines.add(formatLine(entry.name, entry.type, entry.distance, entry.direction, entry.deltaY));
        }
        return lines;
    }

    // Minecraft's -Z is north and +X is east
    private static String direction(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) angle += 360;
        String[] names = {"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};
        return names[(int) Math.round(angle / 45) % 8];
    }

    private record PlayerEntry(String name, PlayerType type, double distance, String direction, double deltaY) {
    }
}
