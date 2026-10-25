package io.github.liam0999.civmod.mixin;

import io.github.liam0999.civmod.player.PlayerSettings;

import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.player.PlayerType;
import io.github.liam0999.civmod.player.PlayerTypeInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class NameTagMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void colorPlayerNameTag(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!(entity instanceof Player) || state.nameTag == null) return;

        if (!CivMod.isEnabled() || !CivMod.config().nametags().isEnabled()) return;

        PlayerSettings players = CivMod.config().players();
        PlayerType playerType = players.getPlayerType(entity.getScoreboardName());
        PlayerTypeInfo typeInfo = players.getPlayerTypeInfo(playerType);
        if (typeInfo == null || !typeInfo.nameTag) return;

        state.nameTag = Component.literal(state.nameTag.getString())
                .withStyle(style -> style.withColor(typeInfo.color.getRGB()));
    }
}
