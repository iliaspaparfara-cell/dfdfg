package com.example.utility.feature;

import com.example.utility.gui.Theme;
import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Shows the name and health of the nearest player. */
public class TargetHud extends Feature {
    private final Setting.Num range = add(new Setting.Num("Range", 10, 3, 32, 1));

    public TargetHud() {
        super("Target HUD", "Name and health of the nearest player.", Category.VISUAL);
    }

    @Override
    public void onHud(GuiGraphics g, Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        LivingEntity t = Aim.nearest(mc, p, range.get(), true);
        if (t == null) return;

        int w = 110, h = 30;
        int x = g.guiWidth() / 2 + 30, y = g.guiHeight() / 2 + 10;
        g.fill(x, y, x + w, y + h, 0xA0000000);
        g.fill(x, y, x + 2, y + h, Theme.accent());

        g.drawString(mc.font, mc.font.plainSubstrByWidth(t.getName().getString(), w - 12), x + 7, y + 4, 0xFFFFFFFF, true);

        float hp = t.getHealth(), max = Math.max(1f, t.getMaxHealth());
        int barW = w - 14;
        g.fill(x + 7, y + 17, x + 7 + barW, y + 22, 0xFF2A2A3A);
        g.fill(x + 7, y + 17, x + 7 + (int) (barW * Math.min(1f, hp / max)), y + 22, Theme.accent());
        String s = String.format("%.1f HP", hp);
        g.drawString(mc.font, s, x + w - 6 - mc.font.width(s), y + 4, 0xFFC4C4D4, false);
    }
}
