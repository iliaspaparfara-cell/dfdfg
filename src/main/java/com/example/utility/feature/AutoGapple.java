package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;

/** When your health drops to the threshold, eats a golden apple from the hotbar, then swaps back. */
public class AutoGapple extends Feature {
    private final Setting.Num health = add(new Setting.Num("Health", 10, 2, 19, 1));

    private boolean eating = false;
    private int previousSlot = -1;
    private int ticks = 0;

    public AutoGapple() {
        super("Auto Gapple", "Eats a golden apple when your health is low.", Category.COMBAT);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (eating && mc.player != null) finish(mc, mc.player);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.screen != null) return;

        if (eating) {
            ticks++;
            if (ticks > 3 && !p.isUsingItem()) finish(mc, p);
            return;
        }

        if (p.getHealth() > health.get() || p.isUsingItem()) return;

        int slot = Hotbar.find(p, s -> s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE));
        if (slot < 0) return;

        previousSlot = Hotbar.selected(p);
        Hotbar.select(p, slot);
        mc.options.keyUse.setDown(true);
        eating = true;
        ticks = 0;
    }

    private void finish(Minecraft mc, LocalPlayer p) {
        mc.options.keyUse.setDown(false);
        if (previousSlot >= 0) Hotbar.select(p, previousSlot);
        previousSlot = -1;
        eating = false;
    }
}
