package io.github.liam0999.civmod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.item.ExtendedItemStack;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin {
    @ModifyVariable(method = "itemCount", at = @At("HEAD"), argsOnly = true)
    protected @Nullable String civmodern$showCompactedItem(String value, @Local(argsOnly = true) ItemStack item) {
        if (value != null) {
            return value;
        } else if (((ExtendedItemStack) (Object) item).isMarkedAsCompacted()) {
            return String.valueOf(item.getCount());
        } else {
            return null;
        }
    }

    @ModifyConstant(method = "itemCount", constant = @Constant(intValue = -1))
    protected int civmodern$colorCompactedItem(int itemColor, @Local(argsOnly = true) ItemStack item) {
        if (((ExtendedItemStack) (Object) item).isMarkedAsCompacted()) {
            return 0xff000000 | CivMod.colors().getCompactedColor();
        } else {
            return itemColor;
        }
    }
}
