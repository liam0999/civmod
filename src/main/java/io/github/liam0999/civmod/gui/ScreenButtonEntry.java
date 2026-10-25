package io.github.liam0999.civmod.gui;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Cloth has no button entry, so this one hosts a vanilla button that opens a screen of ours
public class ScreenButtonEntry extends TooltipListEntry<Object> {
    private static final int BUTTON_WIDTH = 150;

    private final Button button;

    public ScreenButtonEntry(Component fieldName, Component label, Supplier<Screen> screen) {
        super(fieldName, null);
        this.button = Button.builder(label, b -> Minecraft.getInstance().gui.setScreen(screen.get()))
            .size(BUTTON_WIDTH, 20)
            .build();
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor guiGraphics,
        int index,
        int y,
        int x,
        int entryWidth,
        int entryHeight,
        int mouseX,
        int mouseY,
        boolean isHovered,
        float delta
    ) {
        super.extractRenderState(guiGraphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.text(minecraft.font, getDisplayedFieldName(), x, y + 6, getPreferredTextColor());

        this.button.active = isEditable();
        this.button.setX(x + entryWidth - BUTTON_WIDTH);
        this.button.setY(y);
        this.button.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.button);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(this.button);
    }
}
