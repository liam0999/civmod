package io.github.liam0999.civmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.liam0999.civmod.snitch.SnitchManager.getMod;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	@Inject(method = "disconnect", at = @At("HEAD"))
	protected void onDisconnect(Component component, CallbackInfo ci) {
		Minecraft.getInstance().execute(() -> {
			try {
				getMod().handleDisconnectedFromServer();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
}