package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

/** Counts your PvP consumables (totems, gapples, crystals, ...) and shows them on the left. */
public class ItemCounter extends Feature {
    public ItemCounter() {
        super("Item Counter", "Counts totems, gapples, crystals and more.", Category.VISUAL);
    }

    private static int count(LocalPlayer p, Item... items) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var stack = p.getInventory().getItem(i);
            for (Item it : items) {
                if (stack.is(it)) n += stack.getCount();
            }
        }
        return n;
    }

    @Override
    public void onHud(GuiGraphics g, Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;

        Map<String, Integer> lines = new LinkedHashMap<>();
        lines.put("Totems", count(p, Items.TOTEM_OF_UNDYING));
        lines.put("Gapples", count(p, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        lines.put("Crystals", count(p, Items.END_CRYSTAL));
        lines.put("Anchors", count(p, Items.RESPAWN_ANCHOR));
        lines.put("Glowstone", count(p, Items.GLOWSTONE));
        lines.put("Wind Charges", count(p, Items.WIND_CHARGE));
        lines.put("Pearls", count(p, Items.ENDER_PEARL));

        int y = g.guiHeight() / 2 - 40;
        for (Map.Entry<String, Integer> e : lines.entrySet()) {
            if (e.getValue() == 0) continue;
            g.drawString(mc.font, e.getKey() + ": " + e.getValue(), 4, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }
}
