package io.github.liam0999.civmod.mixin;

import io.github.liam0999.civmod.chat.CoordinateLinker;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// both chat features hook the same method; ordering is safe because the predicate is pure.
// anything thrown here escapes into the packet handler and disconnects the client, so
// CoordinateLinker swallows its own failures rather than letting them out.
@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    private static final String ADD_MESSAGE =
        "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V";

    @Inject(method = ADD_MESSAGE, at = @At("HEAD"), cancellable = true)
    private void civmod$suppress(
        Component contents,
        MessageSignature signature,
        GuiMessageSource source,
        GuiMessageTag tag,
        CallbackInfo ci
    ) {
        if (CoordinateLinker.shouldSuppress(contents)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = ADD_MESSAGE, at = @At("HEAD"), argsOnly = true)
    protected Component civmod$linkCoordinates(Component original) {
        return CoordinateLinker.decorate(original);
    }
}
