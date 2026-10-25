package io.github.liam0999.civmod.radar;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.Random;
import java.util.UUID;

public class SoundHelper {
    public static void playSound(String soundEventName, UUID playerKey) {
        if ("none".equalsIgnoreCase(soundEventName))
            return;

        Minecraft minecraft = Minecraft.getInstance();
        float playerPitch = .5f + 1.5f * new Random(playerKey.hashCode()).nextFloat();

        minecraft.player.playSound(SoundEvent.createVariableRangeEvent(Identifier.read("block.note_block." + soundEventName).getOrThrow()), 1, playerPitch);
    }
}
