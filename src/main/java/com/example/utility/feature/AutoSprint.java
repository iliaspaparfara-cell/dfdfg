package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Holds sprint for you whenever you walk forward. */
public class AutoSprint extends Feature {
    private boolean forced = false;

    public AutoSprint() {
        super("Auto Sprint", "Sprints automatically while moving forward.", Category.MOVEMENT);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (forced) mc.options.keySprint.setDown(false);
        forced = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean moving = mc.options.keyUp.isDown() && !player.isShiftKeyDown();
        if (moving) {
            mc.options.keySprint.setDown(true);
            forced = true;
        } else if (forced) {
            mc.options.keySprint.setDown(false);
            forced = false;
        }
    }
}
