package io.github.liam0999.civmod.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.radar.Radar;
import io.github.liam0999.civmod.radar.PlayerListOverlay;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class PositionEditorScreen extends Screen {
    private static final int DIM_COLOR = 0x30101010;
    private static final int OUTLINE_FOCUSED = 0xFFFFFFFF;
    private static final int OUTLINE_HOVER = 0xC0FFFFFF;
    private static final int OUTLINE_IDLE = 0x50FFFFFF;
    private static final int GUIDE = 0x40FFFFFF;
    private static final int GUIDE_SNAPPED = 0xFFFFDD44;
    private static final int PAD = 3;
    private static final int NUDGE = 2;
    private static final int SNAP = 8;

    private static boolean open;

    private final Screen parent;
    private final IntegerSliderEntry radarX;
    private final IntegerSliderEntry radarY;
    private final IntegerSliderEntry listX;
    private final IntegerSliderEntry listY;

    private Target dragging = Target.NONE;
    private Target hovered = Target.NONE;
    private Target focused = Target.RADAR;
    private boolean snappedX;
    private boolean snappedY;
    private int grabOffsetX;
    private int grabOffsetY;

    private enum Target { NONE, RADAR, LIST }

    public PositionEditorScreen(
        Screen parent,
        IntegerSliderEntry radarX,
        IntegerSliderEntry radarY,
        IntegerSliderEntry listX,
        IntegerSliderEntry listY
    ) {
        super(Component.translatable("civmod.config.position.title"));
        this.parent = parent;
        this.radarX = radarX;
        this.radarY = radarY;
        this.listX = listX;
        this.listY = listY;
    }

    public static boolean isOpen() {
        return open;
    }

    @Override
    protected void init() {
        open = true;
        addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    @Override
    public void onClose() {
        open = false;
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void removed() {
        open = false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), DIM_COLOR);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        PlayerListOverlay list = CivMod.radar().getPlayerListOverlay();
        list.resetDrawn();

        // drawn here rather than by the HUD so it sits above the dimmed world
        CivMod.radar().renderOverlay(guiGraphics, Minecraft.getInstance().getDeltaTracker());

        if (CivMod.config().radar().getPlayerListEnabled() && !list.drewThisFrame()) {
            list.renderSample(guiGraphics);
        }

        this.hovered = dragging != Target.NONE ? dragging : targetAt(mouseX, mouseY);

        if (dragging != Target.NONE) {
            guides(guiGraphics);
        }

        outline(guiGraphics, radarBounds(), Target.RADAR);
        if (CivMod.config().radar().getPlayerListEnabled()) {
            outline(guiGraphics, listBounds(), Target.LIST);
        }

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        guiGraphics.centeredText(this.font,
            Component.translatable("civmod.config.position.hint"), this.width / 2, 28, 0xA0A0A0);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void outline(GuiGraphicsExtractor guiGraphics, int[] box, Target target) {
        if (box == null) {
            return;
        }
        int color = focused == target ? OUTLINE_FOCUSED : hovered == target ? OUTLINE_HOVER : OUTLINE_IDLE;
        guiGraphics.outline(box[0] - PAD, box[1] - PAD, box[2] + PAD * 2, box[3] + PAD * 2, color);
    }

    private void guides(GuiGraphicsExtractor guiGraphics) {
        int midX = this.width / 2;
        int midY = this.height / 2;
        guiGraphics.fill(midX, 0, midX + 1, this.height, snappedX ? GUIDE_SNAPPED : GUIDE);
        guiGraphics.fill(0, midY, this.width, midY + 1, snappedY ? GUIDE_SNAPPED : GUIDE);
    }

    private int[] radarBounds() {
        Radar radar = CivMod.radar().getRadar();
        int r = radar.getRadarRadius();
        if (r <= 0) {
            return null;
        }
        return new int[]{radar.getRadarDisplayX() - r, radar.getRadarDisplayY() - r, r * 2, r * 2};
    }

    private int[] listBounds() {
        PlayerListOverlay list = CivMod.radar().getPlayerListOverlay();
        if (list.getLastHeight() == 0) {
            return null;
        }
        return new int[]{list.getLastX(), list.getLastY(), list.getLastWidth(), list.getLastHeight()};
    }

    private Target targetAt(int mouseX, int mouseY) {
        if (CivMod.config().radar().getPlayerListEnabled() && inside(listBounds(), mouseX, mouseY)) {
            return Target.LIST;
        }
        if (inside(radarBounds(), mouseX, mouseY)) {
            return Target.RADAR;
        }
        return Target.NONE;
    }

    private static boolean inside(int[] box, int x, int y) {
        return box != null
            && x >= box[0] - PAD && x <= box[0] + box[2] + PAD
            && y >= box[1] - PAD && y <= box[1] + box[3] + PAD;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        Target target = targetAt(mouseX, mouseY);
        if (target == Target.NONE) {
            return false;
        }
        this.focused = target;

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            reset(target);
            return true;
        }
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        int[] box = target == Target.RADAR ? radarBounds() : listBounds();
        this.dragging = target;
        this.grabOffsetX = mouseX - (box[0] + box[2] / 2);
        this.grabOffsetY = mouseY - (box[1] + box[3] / 2);
        return true;
    }

    // the entries already carry the defaults the config declared, so use those
    private void reset(Target target) {
        if (target == Target.RADAR) {
            radarX.getDefaultValue().ifPresent(radarX::setValue);
            radarY.getDefaultValue().ifPresent(radarY::setValue);
        } else if (target == Target.LIST) {
            listX.getDefaultValue().ifPresent(listX::setValue);
            listY.getDefaultValue().ifPresent(listY::setValue);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = Target.NONE;
        this.snappedX = false;
        this.snappedY = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging == Target.NONE) {
            return super.mouseDragged(event, dragX, dragY);
        }

        int centreX = snap((int) event.x() - grabOffsetX, this.width / 2);
        int centreY = snap((int) event.y() - grabOffsetY, this.height / 2);
        this.snappedX = centreX == this.width / 2;
        this.snappedY = centreY == this.height / 2;

        if (dragging == Target.RADAR) {
            moveRadarTo(centreX, centreY);
        } else {
            moveListTo(centreX, centreY);
        }
        return true;
    }

    // inverse of Radar.calcSettings, which insets the usable area by the radar diameter
    private void moveRadarTo(int centreX, int centreY) {
        int diameter = CivMod.radar().getRadar().getRadarRadius() * 2;
        int radius = diameter / 2;
        int innerWidth = this.width - diameter - 2;
        int innerHeight = this.height - diameter - 2;
        if (innerWidth <= 0 || innerHeight <= 0) {
            return;
        }
        radarX.setValue(fraction((centreX - radius - 1) / (float) innerWidth, 0, 1000));
        radarY.setValue(fraction((centreY - radius - 1) / (float) innerHeight, 0, 1000));
    }

    // the nearby list is positioned relative to the screen centre, so its range is -1..1
    private void moveListTo(int centreX, int centreY) {
        PlayerListOverlay list = CivMod.radar().getPlayerListOverlay();
        int boxW = list.getLastWidth();
        int boxH = list.getLastHeight();

        centreX = Mth.clamp(centreX, boxW / 2, this.width - boxW / 2);
        centreY = Mth.clamp(centreY, boxH / 2, this.height - boxH / 2);

        listX.setValue(fraction(anchorX(centreX, boxW) / (float) this.width, 0, 1000));
        listY.setValue(fraction(anchorY(centreY, boxH) / (float) this.height, 0, 1000));
    }

    // pick the anchor whose own alignment rule reproduces the box we just placed
    private float anchorX(int centreX, int boxW) {
        PlayerListOverlay.Align preferred = PlayerListOverlay.alignFor(centreX / (float) this.width);
        for (PlayerListOverlay.Align align : new PlayerListOverlay.Align[]{
            preferred, PlayerListOverlay.Align.CENTRE,
            PlayerListOverlay.Align.LEFT, PlayerListOverlay.Align.RIGHT
        }) {
            int anchor = switch (align) {
                case LEFT -> centreX - boxW / 2;
                case RIGHT -> centreX + boxW / 2;
                case CENTRE -> centreX;
            };
            if (PlayerListOverlay.alignFor(anchor / (float) this.width) == align) {
                return anchor;
            }
        }
        return centreX;
    }

    private float anchorY(int centreY, int boxH) {
        boolean up = centreY > this.height / 2;
        int anchor = up ? centreY + boxH / 2 : centreY - boxH / 2;
        if (PlayerListOverlay.growsUp(anchor / (float) this.height) == up) {
            return anchor;
        }
        return up ? centreY - boxH / 2 : centreY + boxH / 2;
    }

    private static int snap(int value, int target) {
        return Math.abs(value - target) <= SNAP ? target : value;
    }

    private static int fraction(float value, int min, int max) {
        return Mth.clamp(Math.round(value * 1000f), min, max);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Target target = focused;
        int dx = 0;
        int dy = 0;
        switch (event.key()) {
            case InputConstants.KEY_LEFT -> dx = -1;
            case InputConstants.KEY_RIGHT -> dx = 1;
            case InputConstants.KEY_UP -> dy = -1;
            case InputConstants.KEY_DOWN -> dy = 1;
            default -> {
                return super.keyPressed(event);
            }
        }
        if (target == Target.NONE) {
            return super.keyPressed(event);
        }
        if (target == Target.RADAR) {
            radarX.setValue(Mth.clamp(radarX.getValue() + dx * NUDGE, 0, 1000));
            radarY.setValue(Mth.clamp(radarY.getValue() + dy * NUDGE, 0, 1000));
        } else {
            listX.setValue(Mth.clamp(listX.getValue() + dx * NUDGE, 0, 1000));
            listY.setValue(Mth.clamp(listY.getValue() + dy * NUDGE, 0, 1000));
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
