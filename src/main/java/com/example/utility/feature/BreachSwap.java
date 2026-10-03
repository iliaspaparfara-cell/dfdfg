package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * After you land a sword/axe hit, swaps to a mace (Breach preferred) for a few ticks
 * so your next hit uses it, then swaps back.
 */
public class BreachSwap extends Feature {
    private final Setting.Num holdTicks = add(new Setting.Num("Hold Ticks", 10, 2, 40, 1));
    private final Setting.Bool breachOnly = add(new Setting.Bool("Breach Only", true));

    private boolean pending = false;
    private int hold = 0;
    private int previousSlot = -1;

    public BreachSwap() {
        super("Breach Swap", "Swap to a breach mace right after a sword hit.");
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            onAttack(player, entity);
            return InteractionResult.PASS;
        });
    }

    private void onAttack(Player player, Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (!isEnabled() || player != mc.player || !(target instanceof LivingEntity) || hold > 0) return;
        ItemStack held = player.getMainHandItem();
        if (held.is(ItemTags.SWORDS) || held.is(ItemTags.AXES)) pending = true;
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) restore(mc.player);
        pending = false;
        hold = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (hold > 0) {
            hold--;
            if (hold == 0) restore(player);
            return;
        }

        if (pending) {
            pending = false;
            int slot = Hotbar.find(player,
                    s -> s.is(Items.MACE) && (!breachOnly.get() || Hotbar.hasBreach(s)));
            if (slot < 0 || slot == Hotbar.selected(player)) return;
            previousSlot = Hotbar.selected(player);
            Hotbar.select(player, slot);
            hold = (int) holdTicks.get();
        }
    }

    private void restore(LocalPlayer player) {
        if (previousSlot >= 0) Hotbar.select(player, previousSlot);
        previousSlot = -1;
    }
}
