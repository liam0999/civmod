package io.github.liam0999.civmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import io.github.liam0999.civmod.item.ItemSettings;
import io.github.liam0999.civmod.macro.MacroSettings;
import io.github.liam0999.civmod.nametag.NametagSettings;
import io.github.liam0999.civmod.player.PlayerSettings;
import io.github.liam0999.civmod.radar.RadarSettings;
import io.github.liam0999.civmod.snitch.SnitchSettings;
import org.slf4j.Logger;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CivModConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final TypeAdapter<Color> COLOR_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value("#%06X".formatted(value.getRGB() & 0xffffff));
            }
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            JsonElement element = com.google.gson.internal.Streams.parse(in);
            if (element.isJsonNull()) {
                return null;
            }
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return new Color(primitive.getAsInt());
            }
            String text = primitive.getAsString().trim();
            if (text.startsWith("#")) {
                text = text.substring(1);
            }
            return new Color(Integer.parseInt(text, 16));
        }
    };

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Color.class, COLOR_ADAPTER)
        .create();

    private final RadarSettings radar = new RadarSettings();
    private final PlayerSettings players = new PlayerSettings();
    private final NametagSettings nametags = new NametagSettings();
    private final SnitchSettings snitches = new SnitchSettings();
    private final MacroSettings macros = new MacroSettings();
    private final ItemSettings items = new ItemSettings();

    private transient Path file;

    public static CivModConfig load(Path file) {
        CivModConfig config = null;
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, CivModConfig.class);
            } catch (Exception ex) {
                LOGGER.error("Could not read {}, falling back to defaults", file, ex);
            }
        }
        if (config == null) {
            config = new CivModConfig();
        }
        config.file = file;
        config.radar.setSaveHook(config::save);
        config.players.setSaveHook(config::save);
        config.nametags.setSaveHook(config::save);
        config.radar.afterLoad();
        config.players.afterLoad();
        config.save();
        return config;
    }

    public void save() {
        if (file == null) {
            return;
        }
        radar.beforeSave();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not write {}", file, ex);
        }
    }

    public RadarSettings radar() {
        return radar;
    }

    public PlayerSettings players() {
        return players;
    }

    public NametagSettings nametags() {
        return nametags;
    }

    public SnitchSettings snitches() {
        return snitches;
    }

    public MacroSettings macros() {
        return macros;
    }

    public ItemSettings items() {
        return items;
    }
}
