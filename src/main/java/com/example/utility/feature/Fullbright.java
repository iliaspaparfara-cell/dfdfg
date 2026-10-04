package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Gives you a client-side night vision effect so everything is fully lit. */
public class Fullbright extends Feature {
    private boolean added = false;

    public Fullbright() {
        super("Fullbright", "See clearly in the dark.", Category.VISUAL);
    }

    @Override
    protected void onDisable() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && added) p.removeEffect(MobEffects.NIGHT_VISION);
        added = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (!p.hasEffect(MobEffects.NIGHT_VISION)) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0));
            added = true;
        }
    }
}
