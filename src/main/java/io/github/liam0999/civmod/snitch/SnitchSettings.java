package io.github.liam0999.civmod.snitch;

public final class SnitchSettings {
    private boolean rangeOverlayVisible = false;
    private boolean placementHelperVisible = false;

    public boolean isRangeOverlayVisible() {
        return rangeOverlayVisible;
    }

    public void setRangeOverlayVisible(boolean value) {
        rangeOverlayVisible = value;
    }

    public boolean isPlacementHelperVisible() {
        return placementHelperVisible;
    }

    public void setPlacementHelperVisible(boolean value) {
        placementHelperVisible = value;
    }
}
