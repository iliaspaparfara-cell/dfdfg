package com.example.utility.feature;

import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;

/** Clicks Respawn for you as soon as the death screen shows up. */
public class AutoRespawn extends Feature {
    private final Setting.Num delay = add(new Setting.Num("Delay", 10, 0, 60, 1));

    private int wait = -1;

    public AutoRespawn() {
        super("Auto Respawn", "Respawns automatically after dying.", Category.MISC);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || !(mc.screen instanceof DeathScreen)) {
            wait = -1;
            return;
        }
        if (wait < 0) wait = (int) delay.get();
        if (wait > 0) {
            wait--;
            return;
        }
        player.respawn();
        mc.setScreen(null);
        wait = -1;
    }
}
