package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Clicks for you the moment your crosshair is on a target and your weapon is recharged. */
public class Triggerbot extends Feature {
    private final Setting.Num minCooldown = add(new Setting.Num("Min Cooldown", 0.95, 0, 1, 0.05));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    public Triggerbot() {
        super("Triggerbot", "Auto-attacks when your crosshair is on a target.", Category.COMBAT);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (mc.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target
                && Aim.isValid(player, target, playersOnly.get())
                && player.getAttackStrengthScale(0.5F) >= minCooldown.get()) {
            Aim.click(mc);
        }
    }
}
