package io.github.liam0999.civmod.radar.entity;

import io.github.liam0999.civmod.radar.Indicators;
import io.github.liam0999.civmod.player.PlayerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3x2fStack;

import java.awt.Color;

public class PlayerRadarEntity extends RadarEntity {
    private final PlayerType _playerType;

    public PlayerRadarEntity(Entity entity, EntitySettings settings, PlayerType playerType) {
        super(entity, settings);
        _playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return _playerType;
    }

    @Override
    protected void renderInternal(GuiGraphicsExtractor guiGraphics, float displayX, float displayY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Matrix3x2fStack poseStack = guiGraphics.pose();
        RemotePlayer player = (RemotePlayer) getEntity();
        float rotationYaw = minecraft.player.getViewYRot(partialTicks);
        float scale = getSettings().iconScale * 1.7f;

        poseStack.pushMatrix();
        poseStack.translate(displayX, displayY);
        poseStack.rotate(org.joml.Math.toRadians(rotationYaw));

        if (getSettings().showPlayerAvatar) {
            poseStack.pushMatrix();
            poseStack.scale(scale, scale);
            renderPlayerIcon(guiGraphics, player);
            poseStack.popMatrix();
        }

        if (getSettings().showPlayerNames)
            renderPlayerName(guiGraphics, player);

        poseStack.popMatrix();
    }

    private void renderPlayerIcon(GuiGraphicsExtractor guiGraphics, RemotePlayer player) {
        Identifier skin = player.getSkin().body().texturePath();

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin, -4, -4, 8, 8, 8, 8, 8, 8, 64, 64,
                ARGB.colorFromFloat(getSettings().iconOpacity, 1.0F, 1.0F, 1.0F));
    }

    private void renderPlayerName(GuiGraphicsExtractor guiGraphics, RemotePlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        Matrix3x2fStack poseStack = guiGraphics.pose();

        Color color = _playerType == PlayerType.Ally
                ? getSettings().allyPlayerColor
                : (_playerType == PlayerType.Enemy ? getSettings().enemyPlayerColor : getSettings().neutralPlayerColor);

        poseStack.scale(getSettings().fontScale, getSettings().fontScale, poseStack.pushMatrix());

        String playerName = player.getScoreboardName();

        StringBuilder info = new StringBuilder();
        if (getSettings().showExtraPlayerInfo) {
            double dx = player.getX() - minecraft.player.getX();
            double dz = player.getZ() - minecraft.player.getZ();
            info.append((int) Math.round(Math.hypot(dx, dz))).append("m");
        }
        if (getSettings().showYLevel) {
            if (!info.isEmpty()) info.append(", ");
            info.append("Y").append(player.getBlockY());
        }
        if (!info.isEmpty()) {
            playerName += " (" + info + ")";
        }

        if (getSettings().showYLevel) {
            String arrow = Indicators.verticalArrow(player.getY() - minecraft.player.getY());
            if (!arrow.isEmpty()) {
                playerName += " " + arrow;
            }
        }

        Font font = minecraft.font;
        int yOffset = getSettings().showPlayerAvatar
                ? -4 + (int) ((getSettings().iconScale * getSettings().radarScale + 8))
                : -font.lineHeight / 2;
        int xOffset = -font.width(playerName) / 2;

        guiGraphics.text(font, playerName, xOffset, yOffset, color.getRGB());

        poseStack.popMatrix();
    }
}
