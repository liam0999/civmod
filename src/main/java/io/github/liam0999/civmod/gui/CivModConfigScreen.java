package io.github.liam0999.civmod.gui;

import io.github.liam0999.civmod.Keybinds;
import io.github.liam0999.civmod.config.CivModConfig;
import io.github.liam0999.civmod.gui.screen.EntityScreen;
import io.github.liam0999.civmod.gui.screen.ManagePlayersScreen;
import io.github.liam0999.civmod.gui.screen.PositionEditorScreen;
import io.github.liam0999.civmod.item.ItemSettings;
import io.github.liam0999.civmod.macro.MacroSettings;
import io.github.liam0999.civmod.nametag.NametagSettings;
import io.github.liam0999.civmod.player.PlayerSettings;
import io.github.liam0999.civmod.player.PlayerType;
import io.github.liam0999.civmod.player.PlayerTypeInfo;
import io.github.liam0999.civmod.radar.RadarPreview;
import io.github.liam0999.civmod.radar.RadarSettings;
import io.github.liam0999.civmod.radar.SoundInfo;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class CivModConfigScreen {
    private static final Set<String> WITH_TOOLTIP = Set.of(
        "radar.distance",
        "radar.log_scale",
        "radar.show_extra",
        "radar.nearby.enabled",
        "radar.alerts.joins",
        "radar.alerts.leaves",
        "radar.alerts.radar_enters",
        "radar.alerts.radar_leaves",
        "radar.alerts.player",
        "snitches.alerts",
        "macros.snap_pitch",
        "macros.snap_yaw",
        "macros.stop_hungry",
        "items.color",
        "items.crates",
        "items.uncompacted",
        "items.repair"
    );

    private CivModConfigScreen() {
    }

    public static Screen create(CivModConfig config, Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(key("title"))
            .setSavingRunnable(config::save)
            .transparentBackground();

        ConfigEntryBuilder entries = builder.entryBuilder();
        Screen[] self = new Screen[1];

        radar(builder, entries, config, self);
        nametags(builder, entries, config);
        players(builder, entries, config);
        macros(builder, entries, config);
        items(builder, entries, config);
        keybinds(builder, entries);

        self[0] = builder.build();
        return self[0];
    }

    private static void radar(ConfigBuilder builder, ConfigEntryBuilder e, CivModConfig config, Screen[] self) {
        RadarSettings radar = config.radar();
        PlayerSettings players = config.players();
        ConfigCategory category = builder.getOrCreateCategory(key("category.radar"));

        category.addEntry(toggle(e, "radar.enabled", radar.getEnabled(), true, radar::setEnabled));
        category.addEntry(toggle(e, "radar.pings", radar.getPingsEnabled(), true, radar::setPingsEnabled));
        category.addEntry(toggle(e, "radar.speedometer", radar.getSpeedometerEnabled(), false, radar::setSpeedometerEnabled));
        category.addEntry(toggle(e, "radar.log_scale", radar.getLogScaleEnabled(), false, radar::setLogScaleEnabled));

        category.addEntry(e.startIntSlider(key("radar.distance"), radar.getRadarDistance(), 16, 128)
            .setDefaultValue(128)
            .setTooltip(tooltip("radar.distance"))
            .setSaveConsumer(radar::setRadarDistance)
            .build());

        category.addEntry(new ScreenButtonEntry(
            key("radar.entities"),
            key("radar.entities.button"),
            () -> new EntityScreen(currentScreen(), radar)));

        AbstractConfigListEntry<Integer> size = percent(e, "radar.size", radar.getRadarSize(), 0.35f, 10, 100, radar::setRadarSize);
        AbstractConfigListEntry<Integer> opacity = percent(e, "radar.opacity", radar.getRadarOpacity(), 0.5f, 0, 100, radar::setRadarOpacity);
        AbstractConfigListEntry<Integer> lineOpacity = percent(e, "radar.line_opacity", radar.getLineOpacity(), 1.0f, 0, 100, radar::setLineOpacity);
        AbstractConfigListEntry<Integer> iconScale = percent(e, "radar.icon_scale", radar.getIconScale(), 0.8f, 25, 200, radar::setIconScale);
        AbstractConfigListEntry<Integer> fontScale = percent(e, "radar.font_scale", radar.getFontScale(), 1.4f, 50, 300, radar::setFontScale);
        IntegerSliderEntry posX = fraction(e, "radar.x", radar.getRadarX(), 0f, 0, 1000, radar::setRadarX);
        IntegerSliderEntry posY = fraction(e, "radar.y", radar.getRadarY(), 0f, 0, 1000, radar::setRadarY);
        AbstractConfigListEntry<Integer> color = e.startColorField(key("radar.color"), radar.getRadarColor().getRGB() & 0xffffff)
            .setDefaultValue(0x808080)
            .setSaveConsumer(value -> radar.setRadarColor(new Color(value)))
            .build();
        AbstractConfigListEntry<Boolean> showNames = toggle(e, "radar.show_names", radar.getShowPlayerNames(), true, radar::setShowPlayerNames);
        AbstractConfigListEntry<Boolean> showAvatar = toggle(e, "radar.show_avatar", radar.getShowPlayerAvatar(), true, radar::setShowPlayerAvatar);
        AbstractConfigListEntry<Boolean> showExtra = toggle(e, "radar.show_extra", radar.getShowExtraPlayerInfo(), true, radar::setShowExtraPlayerInfo);
        AbstractConfigListEntry<Boolean> showY = toggle(e, "radar.show_y", radar.getShowYLevel(), true, radar::setShowYLevel);

        AbstractConfigListEntry<Boolean> nearbyEnabled = toggle(e, "radar.nearby.enabled", radar.getPlayerListEnabled(), true, radar::setPlayerListEnabled);
        IntegerSliderEntry nearbyX = fraction(e, "radar.nearby.x", radar.getPlayerListX(), 0.5f, 0, 1000, radar::setPlayerListX);
        IntegerSliderEntry nearbyY = fraction(e, "radar.nearby.y", radar.getPlayerListY(), 0.9f, 0, 1000, radar::setPlayerListY);

        category.addEntry(new ScreenButtonEntry(
            key("radar.position"),
            key("radar.position.button"),
            () -> new PositionEditorScreen(currentScreen(), posX, posY, nearbyX, nearbyY)));

        SubCategoryBuilder appearance = e.startSubCategory(key("radar.appearance"));
        appearance.add(size);
        appearance.add(opacity);
        appearance.add(lineOpacity);
        appearance.add(iconScale);
        appearance.add(fontScale);
        appearance.add(posX);
        appearance.add(posY);
        appearance.add(color);
        appearance.add(showNames);
        appearance.add(showAvatar);
        appearance.add(showExtra);
        appearance.add(showY);
        category.addEntry(appearance.build());

        RadarPreview.set(new RadarPreview.Source() {
            @Override
            public boolean active() {
                return PositionEditorScreen.isOpen()
                    || (self[0] != null && Minecraft.getInstance().gui.screen() == self[0]);
            }

            @Override
            public float radarSize() {
                return size.getValue() / 100f;
            }

            @Override
            public float radarOpacity() {
                return opacity.getValue() / 100f;
            }

            @Override
            public float lineOpacity() {
                return lineOpacity.getValue() / 100f;
            }

            @Override
            public float iconScale() {
                return iconScale.getValue() / 100f;
            }

            @Override
            public float fontScale() {
                return fontScale.getValue() / 100f;
            }

            @Override
            public float radarX() {
                return posX.getValue() / 1000f;
            }

            @Override
            public float radarY() {
                return posY.getValue() / 1000f;
            }

            @Override
            public Color radarColor() {
                return new Color(color.getValue());
            }

            @Override
            public boolean showPlayerNames() {
                return showNames.getValue();
            }

            @Override
            public boolean showPlayerAvatar() {
                return showAvatar.getValue();
            }

            @Override
            public boolean showExtraPlayerInfo() {
                return showExtra.getValue();
            }

            @Override
            public boolean showYLevel() {
                return showY.getValue();
            }

            @Override
            public boolean playerListEnabled() {
                return nearbyEnabled.getValue();
            }

            @Override
            public float playerListX() {
                return nearbyX.getValue() / 1000f;
            }

            @Override
            public float playerListY() {
                return nearbyY.getValue() / 1000f;
            }
        });

        SubCategoryBuilder nearby = e.startSubCategory(key("radar.nearby"));
        nearby.add(nearbyEnabled);
        nearby.add(nearbyX);
        nearby.add(nearbyY);
        for (PlayerType type : PlayerType.values()) {
            PlayerTypeInfo info = players.getPlayerTypeInfo(type);
            nearby.add(e.startBooleanToggle(typeKey("radar.nearby.show", type), info.inPlayerList)
                .setDefaultValue(type != PlayerType.Ally)
                .setSaveConsumer(value -> info.inPlayerList = value)
                .build());
        }
        category.addEntry(nearby.build());

        SubCategoryBuilder alerts = e.startSubCategory(key("radar.alerts"));
        alerts.add(toggle(e, "radar.alerts.joins", radar.getLogJoins(), true, radar::setLogJoins));
        alerts.add(toggle(e, "radar.alerts.leaves", radar.getLogLeaves(), true, radar::setLogLeaves));
        alerts.add(toggle(e, "radar.alerts.radar_enters", radar.getLogRadarEnters(), true, radar::setLogRadarEnters));
        alerts.add(toggle(e, "radar.alerts.radar_leaves", radar.getLogRadarLeaves(), true, radar::setLogRadarLeaves));
        for (PlayerType type : PlayerType.values()) {
            PlayerTypeInfo info = players.getPlayerTypeInfo(type);
            alerts.add(new PingSoundEntry(
                Component.translatable("civmod.config.type." + type.name().toLowerCase()),
                info,
                () -> Optional.of(tooltip("radar.alerts.player"))));
        }
        category.addEntry(alerts.build());

    }

    private static void nametags(ConfigBuilder builder, ConfigEntryBuilder e, CivModConfig config) {
        NametagSettings nametags = config.nametags();
        PlayerSettings players = config.players();
        ConfigCategory category = builder.getOrCreateCategory(key("category.nametags"));

        category.addEntry(toggle(e, "nametags.enabled", nametags.isEnabled(), true, nametags::setEnabled));

        for (PlayerType type : PlayerType.values()) {
            PlayerTypeInfo info = players.getPlayerTypeInfo(type);
            category.addEntry(e.startBooleanToggle(typeKey("nametags.show", type), info.nameTag)
                .setDefaultValue(true)
                .setSaveConsumer(value -> info.nameTag = value)
                .build());
        }
    }

    private static void players(ConfigBuilder builder, ConfigEntryBuilder e, CivModConfig config) {
        PlayerSettings players = config.players();
        ConfigCategory category = builder.getOrCreateCategory(key("category.players"));

        for (PlayerType type : PlayerType.values()) {
            PlayerTypeInfo info = players.getPlayerTypeInfo(type);
            category.addEntry(e.startColorField(typeKey("players.color", type), info.color.getRGB() & 0xffffff)
                .setDefaultValue(defaultColor(type))
                .setSaveConsumer(value -> info.color = new Color(value))
                .build());
        }

        category.addEntry(new ScreenButtonEntry(
            key("players.manage"),
            key("players.manage.button"),
            () -> new ManagePlayersScreen(currentScreen(), players)));
    }

    private static void keybinds(ConfigBuilder builder, ConfigEntryBuilder e) {
        ConfigCategory category = builder.getOrCreateCategory(key("category.keybinds"));

        for (KeyMapping mapping : Keybinds.all()) {
            category.addEntry(e.fillKeybindingField(Component.translatable(mapping.getName()), mapping)
                .setKeySaveConsumer(bound -> {
                    mapping.setKey(bound);
                    KeyMapping.resetMapping();
                    Minecraft.getInstance().options.save();
                })
                .build());
        }
    }

    private static void macros(ConfigBuilder builder, ConfigEntryBuilder e, CivModConfig config) {
        MacroSettings macros = config.macros();
        ConfigCategory category = builder.getOrCreateCategory(key("category.macros"));

        category.addEntry(toggle(e, "macros.snap_pitch", macros.iceRoadPitchCardinalEnabled(), true, macros::setIceRoadPitchCardinalEnabled));
        category.addEntry(toggle(e, "macros.snap_yaw", macros.iceRoadYawCardinalEnabled(), true, macros::setIceRoadYawCardinalEnabled));
        category.addEntry(toggle(e, "macros.auto_eat", macros.isIceRoadAutoEat(), true, macros::setIceRoadAutoEat));
        category.addEntry(toggle(e, "macros.stop_hungry", macros.isIceRoadStop(), true, macros::setIceRoadStop));
    }

    private static void items(ConfigBuilder builder, ConfigEntryBuilder e, CivModConfig config) {
        ItemSettings items = config.items();
        ConfigCategory category = builder.getOrCreateCategory(key("category.items"));

        category.addEntry(e.startColorField(key("items.color"), items.getColor() & 0xffffff)
            .setDefaultValue(0xffff58)
            .setTooltip(tooltip("items.color"))
            .setSaveConsumer(items::setColor)
            .build());
        category.addEntry(toggle(e, "items.crates", items.isCratesAreCompacted(), true, items::setCratesAreCompacted));
        category.addEntry(toggle(e, "items.uncompacted", items.isShowUncompactedCount(), true, items::setShowUncompactedCount));
        category.addEntry(toggle(e, "items.repair", items.isShowRepairCost(), true, items::setShowRepairCost));
    }

    private static AbstractConfigListEntry<Boolean> toggle(
        ConfigEntryBuilder e, String name, boolean current, boolean fallback, Consumer<Boolean> save
    ) {
        return e.startBooleanToggle(key(name), current)
            .setDefaultValue(fallback)
            .setTooltip(tooltip(name))
            .setSaveConsumer(save)
            .build();
    }

    // Cloth has no float slider, so floats ride on an int slider scaled by 100
    // finer than percent() so dragging a HUD element is not limited to 101 positions
    private static IntegerSliderEntry fraction(
        ConfigEntryBuilder e, String name, float current, float fallback, int min, int max, Consumer<Float> save
    ) {
        return e.startIntSlider(key(name), Math.round(current * 1000f), min, max)
            .setDefaultValue(Math.round(fallback * 1000f))
            .setTextGetter(value -> Component.literal(String.format("%.1f%%", value / 10f)))
            .setTooltip(tooltip(name))
            .setSaveConsumer(value -> save.accept(value / 1000f))
            .build();
    }

    private static IntegerSliderEntry percent(
        ConfigEntryBuilder e, String name, float current, float fallback, int min, int max, Consumer<Float> save
    ) {
        return e.startIntSlider(key(name), Math.round(current * 100f), min, max)
            .setDefaultValue(Math.round(fallback * 100f))
            .setTextGetter(value -> Component.literal(value + "%"))
            .setTooltip(tooltip(name))
            .setSaveConsumer(value -> save.accept(value / 100f))
            .build();
    }

    private static SoundInfo sound(String value) {
        SoundInfo info = SoundInfo.getByValue(value);
        return info != null ? info : SoundInfo.getByValue("pling");
    }

    private static int defaultColor(PlayerType type) {
        return switch (type) {
            case Ally -> Color.GREEN.getRGB() & 0xffffff;
            case Enemy -> Color.YELLOW.getRGB() & 0xffffff;
            default -> Color.WHITE.getRGB() & 0xffffff;
        };
    }

    private static Screen currentScreen() {
        return net.minecraft.client.Minecraft.getInstance().gui.screen();
    }

    private static Component key(String name) {
        return Component.translatable("civmod.config." + name);
    }

    private static Component typeKey(String name, PlayerType type) {
        return Component.translatable("civmod.config." + name, Component.translatable("civmod.config.type." + type.name().toLowerCase()));
    }

    private static Component[] tooltip(String name) {
        return WITH_TOOLTIP.contains(name)
            ? new Component[]{Component.translatable("civmod.config." + name + ".tooltip")}
            : new Component[0];
    }
}
