package io.github.liam0999.civmod.item;

public final class ItemSettings {
    private int compactedColor = 0xffff58;
    private boolean cratesAreCompacted = true;
    private boolean showUncompactedCount = true;
    private boolean showRepairCost = true;

    public int getColor() {
        return compactedColor;
    }

    public void setColor(int value) {
        compactedColor = value;
    }

    public boolean isCratesAreCompacted() {
        return cratesAreCompacted;
    }

    public void setCratesAreCompacted(boolean value) {
        cratesAreCompacted = value;
    }

    public boolean isShowUncompactedCount() {
        return showUncompactedCount;
    }

    public void setShowUncompactedCount(boolean value) {
        showUncompactedCount = value;
    }

    public boolean isShowRepairCost() {
        return showRepairCost;
    }

    public void setShowRepairCost(boolean value) {
        showRepairCost = value;
    }
}
