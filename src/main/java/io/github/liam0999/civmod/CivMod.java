package io.github.liam0999.civmod;

import com.google.common.eventbus.Subscribe;
import com.mojang.logging.LogUtils;
import io.github.liam0999.civmod.config.CivModConfig;
import io.github.liam0999.civmod.config.ColorProvider;
import io.github.liam0999.civmod.event.ClientTickEvent;
import io.github.liam0999.civmod.event.EventBus;
import io.github.liam0999.civmod.gui.CivModConfigScreen;
import io.github.liam0999.civmod.macro.AttackMacro;
import io.github.liam0999.civmod.macro.HoldKeyMacro;
import io.github.liam0999.civmod.macro.IceRoadMacro;
import io.github.liam0999.civmod.radar.RadarManager;
import io.github.liam0999.civmod.snitch.SnitchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class CivMod {
    public static final String MOD_ID = "civmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final CivModConfig FALLBACK_CONFIG = new CivModConfig();
    private static final ColorProvider FALLBACK_COLORS = new ColorProvider(FALLBACK_CONFIG);

    private static CivMod instance;

    public final EventBus eventBus = new EventBus("CivModEvents");

    private CivModConfig config;
    private ColorProvider colorProvider;
    private RadarManager radar;
    private SnitchManager snitches;

    public CivMod() {
        if (instance != null) {
            throw new IllegalStateException("CivMod initialised twice");
        }
        instance = this;
    }

    public static CivMod get() {
        return instance;
    }

    public static boolean isEnabled() {
        return instance != null && instance.config != null;
    }

    // mixins can run before the client starts, so fall back to unsaveable defaults
    public static CivModConfig config() {
        return isEnabled() ? instance.config : FALLBACK_CONFIG;
    }

    public static ColorProvider colors() {
        return isEnabled() ? instance.colorProvider : FALLBACK_COLORS;
    }

    public static EventBus events() {
        return instance.eventBus;
    }

    public static RadarManager radar() {
        return instance.radar;
    }

    public static SnitchManager snitches() {
        return instance.snitches;
    }

    public void enable() {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        this.config = CivModConfig.load(gameDir.resolve("config").resolve(MOD_ID + ".json"));
        this.colorProvider = new ColorProvider(this.config);

        this.radar = new RadarManager();
        this.radar.init(this.config.radar(), this.config.players());

        this.snitches = new SnitchManager(this.config.snitches());

        this.eventBus.register(this);

        Options options = Minecraft.getInstance().options;
        this.eventBus.register(new HoldKeyMacro(Keybinds.MACRO_HOLD_LEFT, options.keyAttack));
        this.eventBus.register(new HoldKeyMacro(Keybinds.MACRO_HOLD_RIGHT, options.keyUse));
        this.eventBus.register(new IceRoadMacro(this.config.macros(), Keybinds.MACRO_ICE_ROAD));
        this.eventBus.register(new AttackMacro(Keybinds.MACRO_ATTACK, options.keyAttack));

        LOGGER.info("CivMod enabled");
    }

    @Subscribe
    private void tick(final @NotNull ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (Keybinds.SETTINGS.consumeClick()) {
            if (!minecraft.gui.hud.isHidden() && minecraft.gui.screen() == null) {
                minecraft.gui.setScreen(newConfigScreen(null));
            }
        }
    }

    public @NotNull Screen newConfigScreen(final @Nullable Screen parent) {
        return CivModConfigScreen.create(this.config, parent);
    }

    public CivModConfig getConfig() {
        return this.config;
    }

    public ColorProvider getColorProvider() {
        return this.colorProvider;
    }
}
