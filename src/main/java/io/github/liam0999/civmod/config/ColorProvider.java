package io.github.liam0999.civmod.config;

import java.util.Objects;

public final class ColorProvider {
    private final CivModConfig config;
    private Integer compacted;

    public ColorProvider(CivModConfig config) {
        this.config = config;
    }

    public int getCompactedColor() {
        return Objects.requireNonNullElseGet(compacted, config.items()::getColor);
    }

    public void setTemporaryCompactedColor(Integer color) {
        this.compacted = color;
    }
}
