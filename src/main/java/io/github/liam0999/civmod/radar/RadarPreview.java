package io.github.liam0999.civmod.radar;

import java.awt.Color;

// Lets the settings screen show unsaved appearance values on the live radar.
// Cancelling needs no undo: nothing was written to the settings in the first place.
public final class RadarPreview {
    public interface Source {
        boolean active();

        float radarSize();

        float radarOpacity();

        float lineOpacity();

        float iconScale();

        float fontScale();

        float radarX();

        float radarY();

        Color radarColor();

        boolean showPlayerNames();

        boolean showPlayerAvatar();

        boolean showExtraPlayerInfo();

        boolean showYLevel();

        boolean playerListEnabled();

        float playerListX();

        float playerListY();
    }

    private static Source source;

    private RadarPreview() {
    }

    public static void set(Source value) {
        source = value;
    }

    // not cleared when inactive, so stepping into a sub-screen and back resumes the preview
    public static Source active() {
        return source != null && source.active() ? source : null;
    }
}
