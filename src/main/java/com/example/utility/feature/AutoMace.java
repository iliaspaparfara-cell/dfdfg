package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

/**
 * While you are falling and an enemy is close, equips a mace from the hotbar,
 * clicks (optionally with silent aim), then swaps back to your old slot.
 */
public class AutoMace extends Feature {
    private final Setting.Num minFall = add(new Setting.Num("Min Fall", 1.6, 1.5, 15, 0.1));
    private final Setting.Num equipRange = add(new Setting.Num("Equip Range", 5, 3, 8, 0.5));
    private final Setting.Num reach = add(new Setting.Num("Reach", 3.0, 2, 6, 0.1));
    private final Setting.Num attackDelay = add(new Setting.Num("Attack Delay", 4, 0, 20, 1));
    private final Setting.Bool autoAttack = add(new Setting.Bool("Auto Attack", true));
    private final Setting.Bool silentAim = add(new Setting.Bool("Silent Aim", true));
    private final Setting.Bool swapBack = add(new Setting.Bool("Swap Back", true));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    private int previousSlot = -1;
    private int delayTicks = 0;

    public AutoMace() {
        super("Auto Mace", "Swaps to a mace and smashes while falling.");
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) restore(mc.player);
        delayTicks = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (delayTicks > 0) delayTicks--;

        boolean falling = !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.getAbilities().flying
                && player.fallDistance >= minFall.get();

        if (!falling) {
            restore(player);
            return;
        }

        int maceSlot = findMace(player);
        if (maceSlot < 0) return;

        if (Aim.nearest(mc, player, equipRange.get(), playersOnly.get()) == null) {
            restore(player);
            return;
        }

        equip(player, maceSlot);

        if (!autoAttack.get() || delayTicks > 0) return;

        boolean clicked = false;
        if (silentAim.get()) {
            LivingEntity target = Aim.nearest(mc, player, reach.get(), playersOnly.get());
            if (target != null) clicked = Aim.silentClick(mc, player, target);
        } else if (mc.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target
                && Aim.isValid(player, target, playersOnly.get())) {
            clicked = Aim.click(mc);
        }

        if (clicked) delayTicks = (int) attackDelay.get();
    }

    private int findMace(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.MACE)) return i;
        }
        return -1;
    }

    private void equip(LocalPlayer player, int slot) {
        int current = player.getInventory().getSelectedSlot();
        if (current == slot) return;
        if (previousSlot < 0) previousSlot = current;
        player.getInventory().setSelectedSlot(slot);
    }

    private void restore(LocalPlayer player) {
        if (previousSlot >= 0 && swapBack.get()) {
            player.getInventory().setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
    }
}
