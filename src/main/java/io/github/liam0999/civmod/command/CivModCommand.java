package io.github.liam0999.civmod.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.liam0999.civmod.CivMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public final class CivModCommand {
    private CivModCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal(CivMod.MOD_ID)
            .executes(context -> {
                openSettings();
                return 1;
            })
            .then(ClientCommands.literal("settings")
                .executes(context -> {
                    openSettings();
                    return 1;
                }))
            .then(ClientCommands.literal("players")
                .then(ClientCommands.literal("allies").executes(context -> {
                    CivMod.radar().reportOnlinePlayers(true, false);
                    return 1;
                }))
                .then(ClientCommands.literal("enemies").executes(context -> {
                    CivMod.radar().reportOnlinePlayers(false, true);
                    return 1;
                }))
                .then(ClientCommands.literal("all").executes(context -> {
                    CivMod.radar().reportOnlinePlayers(true, true);
                    return 1;
                })))
            .then(ClientCommands.literal("snitches")
                .then(ClientCommands.literal("cleargone").executes(context -> {
                    CivMod.snitches().clearGoneSnitches();
                    return 1;
                }))));
    }

    private static void openSettings() {
        // deferred so the closing chat screen does not replace the settings screen
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.gui.setScreen(CivMod.get().newConfigScreen(null)));
    }
}
