package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureManager {
    private static final List<Feature> FEATURES = new ArrayList<>();

    public static void init() {
        FEATURES.add(new AutoMace());
        FEATURES.add(new SilentAim());
        FEATURES.add(new BreachSwap());
        FEATURES.add(new WindChargeMacro());
        FEATURES.add(new InstantLunge());
        FEATURES.add(new AutoPearlCatch());
        FEATURES.add(new ShieldDrain());
        FEATURES.add(new AutoAnchor());
        FEATURES.add(new CrystalAura());
        FEATURES.add(new SpearAura());
        FEATURES.add(new AutoTotem());
        FEATURES.add(new AutoRespawn());
        FEATURES.add(new AutoSprint());
        FEATURES.add(new AutoJump());
        FEATURES.add(new ModuleList());
        FEATURES.add(new FallHud());
        FEATURES.add(new Fullbright());
        FEATURES.add(new Esp());
        FEATURES.add(new Triggerbot());
        FEATURES.add(new AutoGapple());
        FEATURES.add(new ArmorHud());
        FEATURES.add(new TargetHud());
        FEATURES.add(new ItemCounter());
        // Register more features here.
    }

    public static List<Feature> all() {
        return Collections.unmodifiableList(FEATURES);
    }

    public static void renderHud(GuiGraphics g, Minecraft mc) {
        if (mc.player == null) return;
        for (Feature f : FEATURES) {
            if (f.isEnabled()) f.onHud(g, mc);
        }
    }

    public static void tick(Minecraft mc) {
        for (Feature f : FEATURES) {
            if (f.isEnabled()) f.onTick(mc);
        }
    }
}
