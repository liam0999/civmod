package io.github.liam0999.civmod;

import io.github.liam0999.civmod.command.CivModCommand;
import io.github.liam0999.civmod.event.ChatEvent;
import io.github.liam0999.civmod.event.ClientTickEvent;
import io.github.liam0999.civmod.radar.RadarManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CivModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CivMod mod = new CivMod();

        Keybinds.register();

        RenderPipelines.register(RadarManager.CIRCLE);
        RenderPipelines.register(RadarManager.TRIANGLES);
        RenderPipelines.register(RadarManager.BORDER);
        RenderPipelines.register(RadarManager.LINES);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            mod.enable();

            HudElementRegistry.attachElementAfter(
                VanillaHudElements.SUBTITLES,
                Identifier.fromNamespaceAndPath(CivMod.MOD_ID, "overlay"),
                CivMod.radar()::render
            );
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!CivMod.isEnabled()) {
                return;
            }
            CivMod.events().post(new ClientTickEvent());
            CivMod.radar().tick();
            CivMod.snitches().handleTick();
        });

        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            if (!CivMod.isEnabled()) {
                return;
            }
            try {
                CivMod.snitches().handleRenderBlockOverlay(
                    context.poseStack(),
                    context.submitNodeCollector(),
                    context.levelState().cameraRenderState
                );
            } catch (Throwable ex) {
                CivMod.LOGGER.error("Snitch overlay render failed", ex);
            }
        });

        ChatEvent.EVENT.register(component -> CivMod.isEnabled() && CivMod.radar().processChat(component));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            CivModCommand.register(dispatcher));
    }
}
