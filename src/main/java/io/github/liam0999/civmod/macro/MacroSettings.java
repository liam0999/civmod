package io.github.liam0999.civmod.macro;

public final class MacroSettings {
    private boolean iceRoadPitchCardinal = true;
    private boolean iceRoadYawCardinal = true;
    private boolean iceRoadAutoEat = true;
    private boolean iceRoadStop = true;

    public boolean iceRoadPitchCardinalEnabled() {
        return iceRoadPitchCardinal;
    }

    public void setIceRoadPitchCardinalEnabled(boolean value) {
        iceRoadPitchCardinal = value;
    }

    public boolean iceRoadYawCardinalEnabled() {
        return iceRoadYawCardinal;
    }

    public void setIceRoadYawCardinalEnabled(boolean value) {
        iceRoadYawCardinal = value;
    }

    public boolean isIceRoadAutoEat() {
        return iceRoadAutoEat;
    }

    public void setIceRoadAutoEat(boolean value) {
        iceRoadAutoEat = value;
    }

    public boolean isIceRoadStop() {
        return iceRoadStop;
    }

    public void setIceRoadStop(boolean value) {
        iceRoadStop = value;
    }
}
