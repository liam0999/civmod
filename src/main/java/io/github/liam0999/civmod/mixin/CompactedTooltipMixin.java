package io.github.liam0999.civmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.item.ExtendedItemStack;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class CompactedTooltipMixin {
    private static final int UNSTACKABLE_COMPACT_RATIO = 8;

    @ModifyReturnValue(at = @At("RETURN"), method = "getTooltipLines")
    protected List<Component> civmodern$showUncompactedCount(List<Component> original) {
        ItemStack itemStack = (ItemStack) (Object) this;

        if (!CivMod.config().items().isShowUncompactedCount()) {
            return original;
        }

        if (!((ExtendedItemStack) (Object) itemStack).isMarkedAsCompacted()) {
            return original;
        }

        // a crate holds items rather than being a compacted stack, so it only gets the recolor
        int loreIndex = compactedLoreIndex(original);
        if (loreIndex < 0) {
            return original;
        }

        int uncompacted = itemStack.getCount() * compactRatio(itemStack);
        original.add(loreIndex + 1, Component.translatable("civmod.compactedcount", uncompacted)
            .withColor(CivMod.config().items().getColor()));

        return original;
    }

    private static int compactedLoreIndex(List<Component> tooltip) {
        for (int i = 1; i < tooltip.size(); i++) {
            if (ExtendedItemStack.COMPACTED_ITEM_LORE.equals(tooltip.get(i).getString())) {
                return i;
            }
        }
        return -1;
    }

    // a compacted item holds one stack of the base item, except unstackables which hold 8
    private static int compactRatio(ItemStack itemStack) {
        int stackSize = itemStack.getItem().getDefaultMaxStackSize();
        return stackSize > 1 ? stackSize : UNSTACKABLE_COMPACT_RATIO;
    }
}
