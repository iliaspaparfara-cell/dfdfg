package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hold the attack button: the nearest target in reach gets hit with a real click
 * while your camera stays exactly where it is.
 */
public class SilentAim extends Feature {
    private final Setting.Num reach = add(new Setting.Num("Reach", 3.0, 2, 6, 0.1));
    private final Setting.Num minCooldown = add(new Setting.Num("Min Cooldown", 0.9, 0, 1, 0.05));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    public SilentAim() {
        super("Silent Aim", "Hold attack to hit the nearest target without turning.");
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (player.getAttackStrengthScale(0.5F) < minCooldown.get()) return;

        LivingEntity target = Aim.nearest(mc, player, reach.get(), playersOnly.get());
        if (target != null) Aim.silentClick(mc, player, target);
    }
}
