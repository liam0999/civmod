package io.github.liam0999.civmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.liam0999.civmod.snitch.SnitchManager.getMod;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
	protected void onHandleChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
		if (!Minecraft.getInstance().isSameThread()) {
			// waiting for mc to call this again from the mc thread, so we can cancel it properly
			return;
		}
		try {
			boolean dropPacket = getMod().handleChat(packet.content());
			if (dropPacket) ci.cancel();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Inject(method = "handleContainerContent", at = @At("HEAD"))
	protected void onHandleContainerContent(ClientboundContainerSetContentPacket packetIn, CallbackInfo ci) {
		if (!Minecraft.getInstance().isSameThread()) {
			// waiting for mc to call this again from the mc thread
			return;
		}
		try {
			getMod().handleWindowItems(packetIn.items());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Inject(method = "handleLogin", at = @At("HEAD"))
	protected void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		if (!Minecraft.getInstance().isSameThread()) {
			// waiting for mc to call this again from the mc thread, so we can cancel it properly
			return;
		}
		Minecraft.getInstance().execute(() -> {
			try {
				getMod().handleConnectedToServer();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
}
