package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/**
 * While an enemy in reach is blocking with a shield and you hold attack,
 * keeps landing hits on them every few ticks to wear the shield down.
 */
public class ShieldDrain extends Feature {
    private final Setting.Num interval = add(new Setting.Num("Click Interval", 2, 1, 10, 1));
    private final Setting.Num reach = add(new Setting.Num("Reach", 3.0, 2, 6, 0.1));
    private final Setting.Bool silentAim = add(new Setting.Bool("Silent Aim", true));
    private final Setting.Bool holdToUse = add(new Setting.Bool("Hold Attack", true));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    private int cd = 0;

    public ShieldDrain() {
        super("Shield Drain", "Repeatedly hits a blocking enemy's shield.");
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) cd--;
        if (cd > 0) return;
        if (holdToUse.get() && !mc.options.keyAttack.isDown()) return;

        boolean clicked = false;
        if (silentAim.get()) {
            LivingEntity target = Aim.nearest(mc, player, reach.get(), playersOnly.get(), LivingEntity::isBlocking);
            if (target != null) clicked = Aim.silentClick(mc, player, target);
        } else if (mc.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target
                && Aim.isValid(player, target, playersOnly.get())
                && target.isBlocking()) {
            clicked = Aim.click(mc);
        }

        if (clicked) cd = (int) interval.get();
    }
}
