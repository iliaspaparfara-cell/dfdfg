package com.example.utility.feature;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureManager {
    private static final List<Feature> FEATURES = new ArrayList<>();

    public static void init() {
        FEATURES.add(new AutoMace());
        FEATURES.add(new SilentAim());
        // Register more features here.
    }

    public static List<Feature> all() {
        return Collections.unmodifiableList(FEATURES);
    }

    public static void tick(Minecraft mc) {
        for (Feature f : FEATURES) {
            if (f.isEnabled()) f.onTick(mc);
        }
    }
}
