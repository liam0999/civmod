package io.github.liam0999.civmod.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.liam0999.civmod.CivMod;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (previousScreen) -> CivMod.get().newConfigScreen(previousScreen);
    }
}
