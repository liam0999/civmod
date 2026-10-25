package io.github.liam0999.civmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.liam0999.civmod.chat.CoordinateLink;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    // shift click is vanilla's insertion mode, which drops click events entirely
    @Inject(method = "handleComponentClicked(Lnet/minecraft/network/chat/Style;Z)Z", at = @At("HEAD"), cancellable = true)
    private void civmodern$handleCoordinateClick(Style style, boolean insertion, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (CoordinateLink.handleClick(style.getClickEvent(), minecraft, (Screen) (Object) this, insertion)) {
            cir.setReturnValue(true);
        }
    }
}
