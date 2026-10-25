package io.github.liam0999.civmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.liam0999.civmod.CivMod;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemRepairMixin {
    @ModifyReturnValue(at = @At("RETURN"), method = "getTooltipLines")
    protected List<Component> handle(List<Component> original) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (CivMod.config().items().isShowRepairCost() && itemStack.get(DataComponents.REPAIRABLE) != null) {
            Integer repairCost = itemStack.get(DataComponents.REPAIR_COST);
            if (repairCost != null) {
                original.add(1, Component.translatable("civmod.repaircost", repairCost + 2).withColor(0x379fa3));
            }
        }
        return original;
    }
}
