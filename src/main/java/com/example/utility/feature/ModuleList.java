package com.example.utility.feature;

import com.example.utility.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;

/** HUD list of your enabled modules in the top-right corner. */
public class ModuleList extends Feature {
    public ModuleList() {
        super("Module List", "Shows enabled modules on screen.", Category.VISUAL);
    }

    @Override
    public void onHud(GuiGraphics g, Minecraft mc) {
        List<String> names = FeatureManager.all().stream()
                .filter(Feature::isEnabled)
                .map(Feature::getName)
                .sorted(Comparator.comparingInt((String n) -> mc.font.width(n)).reversed())
                .toList();

        int right = g.guiWidth();
        int y = 4;
        for (String n : names) {
            int x = right - 6 - mc.font.width(n);
            g.fill(x - 3, y - 1, right - 2, y + 10, 0x90000000);
            g.fill(right - 2, y - 1, right, y + 10, Theme.accent());
            g.drawString(mc.font, n, x, y, Theme.accent(), true);
            y += 11;
        }
    }
}
