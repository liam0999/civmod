package io.github.liam0999.civmod.mixin;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.event.ScrollEvent;

@Mixin(Inventory.class)
public class InventoryMixin {
  @Shadow
  public int selected;

  @Inject(method = "setSelectedSlot", at = @At("HEAD"))
  protected void setSelectedHotbarSlot(int slot, CallbackInfo info) {
    if (slot != this.selected) {
      CivMod.events().post(new ScrollEvent(this.selected == (slot + 1) % Inventory.getSelectionSize()));
    }
  }
}
