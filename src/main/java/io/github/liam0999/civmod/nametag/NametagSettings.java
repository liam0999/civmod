package io.github.liam0999.civmod.nametag;

public final class NametagSettings {
    private boolean enabled = true;

    private transient Runnable saveHook;

    public void setSaveHook(Runnable hook) {
        this.saveHook = hook;
    }

    public void save() {
        if (saveHook != null) {
            saveHook.run();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }
}
