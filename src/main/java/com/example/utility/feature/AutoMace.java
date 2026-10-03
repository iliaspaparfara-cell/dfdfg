package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

/**
 * While you are falling and an enemy is close, equips a mace from the hotbar,
 * clicks (optionally with silent aim), then swaps back to your old slot.
 * Stun Slam: if the target is blocking with a shield, hit with an axe first, then the mace.
 */
public class AutoMace extends Feature {
    private final Setting.Num minFall = add(new Setting.Num("Min Fall", 1.6, 1.5, 15, 0.1));
    private final Setting.Num equipRange = add(new Setting.Num("Equip Range", 5, 3, 8, 0.5));
    private final Setting.Num reach = add(new Setting.Num("Reach", 3.0, 2, 6, 0.1));
    private final Setting.Num attackDelay = add(new Setting.Num("Attack Delay", 4, 0, 20, 1));
    private final Setting.Bool autoAttack = add(new Setting.Bool("Auto Attack", true));
    private final Setting.Bool silentAim = add(new Setting.Bool("Silent Aim", true));
    private final Setting.Bool stunSlam = add(new Setting.Bool("Stun Slam", true));
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

        int maceSlot = Hotbar.find(player, s -> s.is(Items.MACE));
        if (maceSlot < 0) return;

        if (Aim.nearest(mc, player, equipRange.get(), playersOnly.get()) == null) {
            restore(player);
            return;
        }

        // Pick the target we would hit this tick.
        LivingEntity target = null;
        if (silentAim.get()) {
            target = Aim.nearest(mc, player, reach.get(), playersOnly.get());
        } else if (mc.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity e
                && Aim.isValid(player, e, playersOnly.get())) {
            target = e;
        }

        // Stun slam: break the shield with an axe first.
        int axeSlot = stunSlam.get() ? Hotbar.find(player, s -> s.is(ItemTags.AXES)) : -1;
        boolean stun = target != null && axeSlot >= 0 && target.isBlocking();

        equip(player, stun ? axeSlot : maceSlot);

        if (!autoAttack.get() || delayTicks > 0 || target == null) return;

        boolean clicked = silentAim.get()
                ? Aim.silentClick(mc, player, target)
                : Aim.click(mc);

        if (clicked) delayTicks = stun ? 1 : (int) attackDelay.get();
    }

    private void equip(LocalPlayer player, int slot) {
        int current = Hotbar.selected(player);
        if (current == slot) return;
        if (previousSlot < 0) previousSlot = current;
        Hotbar.select(player, slot);
    }

    private void restore(LocalPlayer player) {
        if (previousSlot >= 0 && swapBack.get()) {
            Hotbar.select(player, previousSlot);
        }
        previousSlot = -1;
    }
}
