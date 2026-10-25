package io.github.liam0999.civmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.liam0999.civmod.chat.CoordinateLink;

@Mixin(Screen.class)
public class CoordinateClickMixin {
    // vanilla would forward a Custom click to the server, so this has to cancel
    @Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
    private static void civmodern$handleCoordinateClick(ClickEvent clickEvent, Minecraft minecraft, Screen screen, CallbackInfo ci) {
        if (CoordinateLink.handleClick(clickEvent, minecraft, screen, minecraft.hasShiftDown())) {
            ci.cancel();
        }
    }
}
