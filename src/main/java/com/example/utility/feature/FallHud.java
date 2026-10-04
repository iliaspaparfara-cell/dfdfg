package com.example.utility.feature;

import com.example.utility.gui.Theme;
import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/** Shows your fall distance and whether a mace smash would currently land. */
public class FallHud extends Feature {
    private final Setting.Bool showCoords = add(new Setting.Bool("Show Coords", true));

    public FallHud() {
        super("Fall HUD", "Fall distance + mace smash indicator.", Category.VISUAL);
    }

    @Override
    public void onHud(GuiGraphics g, Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;

        double fall = p.fallDistance;
        boolean ready = fall > 1.5 && !p.onGround();

        String line = String.format("Fall: %.1f", fall);
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2 + 24;
        g.drawString(mc.font, line, cx - mc.font.width(line) / 2, cy, ready ? 0xFF55FF55 : 0xFFFFFFFF, true);
        if (ready) {
            String s = "SMASH READY";
            g.drawString(mc.font, s, cx - mc.font.width(s) / 2, cy + 11, Theme.accent(), true);
        }

        if (showCoords.get()) {
            String xyz = String.format("XYZ: %.0f %.0f %.0f", p.getX(), p.getY(), p.getZ());
            g.drawString(mc.font, xyz, 4, g.guiHeight() - 12, 0xFFFFFFFF, true);
        }
    }
}
