package com.example.utility.feature;

import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Feature {
    private final String name;
    private final String description;
    private final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Feature(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Setting> getSettings() { return settings; }
    public boolean isEnabled() { return enabled; }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (enabled) onEnable(); else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(Minecraft mc) {}
}
