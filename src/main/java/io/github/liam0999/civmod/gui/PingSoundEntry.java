package io.github.liam0999.civmod.gui;

import io.github.liam0999.civmod.player.PlayerTypeInfo;
import io.github.liam0999.civmod.radar.SoundHelper;
import io.github.liam0999.civmod.radar.SoundInfo;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Cloth gives each entry its own row, so the toggle and the sound picker share one custom entry
public class PingSoundEntry extends TooltipListEntry<Boolean> {
    private static final int CONTROLS_WIDTH = 150;
    private static final int TOGGLE_WIDTH = 42;
    private static final int GAP = 4;

    private final PlayerTypeInfo info;
    private final boolean originalPing;
    private final String originalSound;

    private Button toggleButton;
    private Button soundButton;

    private boolean ping;
    private int soundIndex;

    public PingSoundEntry(Component fieldName, PlayerTypeInfo info, Supplier<Optional<Component[]>> tooltip) {
        super(fieldName, tooltip);
        this.info = info;
        this.originalPing = info.ping;
        this.originalSound = info.soundEventName;
        this.ping = info.ping;
        this.soundIndex = indexOf(info.soundEventName);

        this.toggleButton = Button.builder(toggleLabel(), b -> {
            this.ping = !this.ping;
            this.toggleButton.setMessage(toggleLabel());
        }).size(TOGGLE_WIDTH, 20).build();

        this.soundButton = Button.builder(soundLabel(), b -> {
            this.soundIndex = (this.soundIndex + 1) % SoundInfo.SOUND_LIST.length;
            this.soundButton.setMessage(soundLabel());
            preview();
        }).size(CONTROLS_WIDTH - TOGGLE_WIDTH - GAP, 20).build();
    }

    private static int indexOf(String value) {
        for (int i = 0; i < SoundInfo.SOUND_LIST.length; i++) {
            if (SoundInfo.SOUND_LIST[i].value.equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private Component toggleLabel() {
        return ping ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    private Component soundLabel() {
        return Component.literal(SoundInfo.SOUND_LIST[soundIndex].name);
    }

    private void preview() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && ping) {
            SoundHelper.playSound(SoundInfo.SOUND_LIST[soundIndex].value, minecraft.player.getUUID());
        }
    }

    @Override
    public Boolean getValue() {
        return ping;
    }

    @Override
    public Optional<Boolean> getDefaultValue() {
        return Optional.of(true);
    }

    @Override
    public boolean isEdited() {
        return ping != originalPing || !SoundInfo.SOUND_LIST[soundIndex].value.equals(originalSound);
    }

    @Override
    public void save() {
        info.ping = ping;
        info.soundEventName = SoundInfo.SOUND_LIST[soundIndex].value;
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

        int right = x + entryWidth - CONTROLS_WIDTH;
        this.toggleButton.active = isEditable();
        this.toggleButton.setX(right);
        this.toggleButton.setY(y);
        this.toggleButton.extractRenderState(guiGraphics, mouseX, mouseY, delta);

        this.soundButton.active = isEditable() && ping;
        this.soundButton.setX(right + TOGGLE_WIDTH + GAP);
        this.soundButton.setY(y);
        this.soundButton.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.toggleButton, this.soundButton);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(this.toggleButton, this.soundButton);
    }
}
