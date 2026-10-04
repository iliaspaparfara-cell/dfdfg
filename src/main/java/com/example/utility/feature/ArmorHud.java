package com.example.utility.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Shows your worn armor with durability percentages on the right side of the screen. */
public class ArmorHud extends Feature {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ArmorHud() {
        super("Armor HUD", "Worn armor + durability on screen.", Category.VISUAL);
    }

    @Override
    public void onHud(GuiGraphics g, Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;

        int x = g.guiWidth() - 22;
        int y = g.guiHeight() / 2 - 36;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = p.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            g.renderItem(stack, x, y);
            g.renderItemDecorations(mc.font, stack, x, y);

            if (stack.getMaxDamage() > 0) {
                int pct = (int) Math.round(100.0 * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage());
                int color = pct > 60 ? 0xFF55FF55 : pct > 30 ? 0xFFFFFF55 : 0xFFFF5555;
                String s = pct + "%";
                g.drawString(mc.font, s, x - 4 - mc.font.width(s), y + 4, color, true);
            }
            y += 18;
        }
    }
}
