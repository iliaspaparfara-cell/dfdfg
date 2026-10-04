package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Jumps for you every time you touch the ground while moving (bunny-hop style). */
public class AutoJump extends Feature {
    private boolean forced = false;

    public AutoJump() {
        super("Auto Jump", "Jumps automatically while you move.", Category.MOVEMENT);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (forced) mc.options.keyJump.setDown(false);
        forced = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) return;

        boolean moving = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();

        if (moving && player.onGround() && !player.isShiftKeyDown()) {
            mc.options.keyJump.setDown(true);
            forced = true;
        } else if (forced) {
            mc.options.keyJump.setDown(false);
            forced = false;
        }
    }
}
