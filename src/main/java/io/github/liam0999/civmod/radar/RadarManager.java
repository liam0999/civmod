package io.github.liam0999.civmod.radar;

import io.github.liam0999.civmod.player.PlayerStatusReport;
import io.github.liam0999.civmod.player.PlayerSettings;
import io.github.liam0999.civmod.util.ChatFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.gui.screen.PositionEditorScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class RadarManager {
    private static final RenderPipeline.Snippet UNIFORM_SNIPPET =
        RenderPipeline.builder()
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .buildSnippet();

    public static final RenderPipeline TRIANGLES = pipeline("triangles", PrimitiveTopology.TRIANGLES);
    public static final RenderPipeline LINES = pipeline("lines", PrimitiveTopology.QUADS);
    public static final RenderPipeline CIRCLE = pipeline("circle", PrimitiveTopology.TRIANGLE_FAN);
    public static final RenderPipeline BORDER = pipeline("border", PrimitiveTopology.TRIANGLE_STRIP);

    private RadarSettings config;
    private Radar radar;
    private Speedometer speedometer;
    private PlayerListOverlay playerListOverlay;
    private PlayerStatusReport playerStatusReport;

    private static RenderPipeline pipeline(String name, PrimitiveTopology topology) {
        return RenderPipeline.builder(UNIFORM_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(CivMod.MOD_ID, "pipelines/" + name))
            .withPrimitiveTopology(topology)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .build();
    }

    public void init(RadarSettings config, PlayerSettings players) {
        this.config = config;

        config.setIsJourneyMapEnabled(isModPresent("journeymap.common.Journeymap", "JourneyMap"));
        config.setIsVoxelMapEnabled(isModPresent("com.mamiyaotaru.voxelmap.VoxelMap", "VoxelMap"));
        config.setIsXaerosEnabled(isModPresent("xaero.minimap.XaeroMinimap", "Xaeros Minimap"));

        this.radar = new Radar(config, players);
        this.speedometer = new Speedometer();
        this.playerListOverlay = new PlayerListOverlay(config, players);
        this.playerStatusReport = new PlayerStatusReport(players);
    }

    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (config.getPingsEnabled() || config.getEnabled()) {
            radar.scanEntities();
        }

        if (config.getEnabled()) {
            radar.calcSettings();

            if (config.getSpeedometerEnabled()) {
                speedometer.calc();
            }
        }

        if (config.getPingsEnabled()) {
            radar.sendMessages();
            radar.playSounds();
        }
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (PositionEditorScreen.isOpen()) {
            return;
        }
        renderOverlay(guiGraphics, deltaTracker);
    }

    public void renderOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null
            || minecraft.gui.hud.isHidden()
            || minecraft.debugEntries.isOverlayVisible()
        ) {
            return;
        }

        if (config.getPlayerListEnabled()) {
            playerListOverlay.render(guiGraphics, radar.getPlayerEntities());
        }

        if (!config.getEnabled()) {
            return;
        }

        radar.render(guiGraphics, deltaTracker);

        if (config.getSpeedometerEnabled()) {
            speedometer.render(guiGraphics, radar.getRadarDisplayX(), radar.getRadarDisplayY(), radar.getRadarRadius());
        }
    }

    // must stay pure: the chat mixin may evaluate this twice for the same message
    public boolean processChat(Component message) {
        if (!config.getLogPlayerStatus() || message == null || ChatFormat.isRadarMessage(message)) {
            return false;
        }

        TextColor color1 = message.getStyle().getColor();

        List<Component> siblings = message.getSiblings();
        TextColor color2 = !siblings.isEmpty() ? siblings.getFirst().getStyle().getColor() : null;

        TextColor yellow = TextColor.fromLegacyFormat(ChatFormatting.YELLOW);

        if (yellow.equals(color1) || yellow.equals(color2)) {
            String messageText = message.getString();
            return messageText.contains(" joined the game") || messageText.contains(" left the game");
        }

        return false;
    }

    private static boolean isModPresent(String className, String label) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ex) {
            CivMod.LOGGER.info("{} not found", label);
            return false;
        }
        CivMod.LOGGER.info("{} found", label);
        return true;
    }

    public void reportOnlinePlayers(boolean includeAllies, boolean includeEnemies) {
        playerStatusReport.report(includeAllies, includeEnemies);
    }

    public RadarSettings getConfig() {
        return config;
    }

    public Speedometer getSpeedometer() {
        return speedometer;
    }

    public Radar getRadar() {
        return radar;
    }

    public PlayerListOverlay getPlayerListOverlay() {
        return playerListOverlay;
    }
}
