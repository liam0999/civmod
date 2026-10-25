package io.github.liam0999.civmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class Keybinds {
    public static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CivMod.MOD_ID, "keybind"));

    public static final KeyMapping SETTINGS = of("settings", InputConstants.KEY_R);

    public static final KeyMapping MACRO_HOLD_LEFT = of("macro.hold_left", InputConstants.KEY_MINUS);
    public static final KeyMapping MACRO_HOLD_RIGHT = of("macro.hold_right", InputConstants.KEY_EQUALS);
    public static final KeyMapping MACRO_ICE_ROAD = of("macro.ice_road", InputConstants.KEY_BACKSPACE);
    public static final KeyMapping MACRO_ATTACK = of("macro.attack", InputConstants.KEY_0);

    public static final KeyMapping SNITCH_LIST = of("snitch.list", InputConstants.KEY_L);
    public static final KeyMapping SNITCH_RANGE_OVERLAY = of("snitch.range_overlay", InputConstants.KEY_O);
    public static final KeyMapping SNITCH_PLACEMENT_HELPER = of("snitch.placement_helper", InputConstants.KEY_P);
    public static final KeyMapping SNITCH_FIELD_PREVIEW = of("snitch.field_preview", InputConstants.KEY_N);
    public static final KeyMapping SNITCH_TOGGLE_GONE = of("snitch.toggle_gone", InputConstants.KEY_DELETE);
    public static final KeyMapping JALIST_AUTO = of("jalist.auto", InputConstants.KEY_J);

    private static final KeyMapping[] ALL = {
        SETTINGS,
        MACRO_HOLD_LEFT, MACRO_HOLD_RIGHT, MACRO_ICE_ROAD, MACRO_ATTACK,
        SNITCH_LIST, SNITCH_RANGE_OVERLAY, SNITCH_PLACEMENT_HELPER, SNITCH_FIELD_PREVIEW,
        SNITCH_TOGGLE_GONE, JALIST_AUTO,
    };

    private Keybinds() {
    }

    private static KeyMapping of(String name, int key) {
        return new KeyMapping("key." + CivMod.MOD_ID + "." + name, InputConstants.Type.KEYBOARD, key, CATEGORY);
    }

    public static KeyMapping[] all() {
        return ALL.clone();
    }

    public static void register() {
        for (KeyMapping mapping : ALL) {
            KeyMappingHelper.registerKeyMapping(mapping);
        }
    }
}
