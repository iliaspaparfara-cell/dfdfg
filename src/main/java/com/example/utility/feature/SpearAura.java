package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/** While holding a spear, jabs the nearest target in reach (silently aimed) every few ticks. */
public class SpearAura extends Feature {
    private final Setting.Num reach = add(new Setting.Num("Reach", 4.0, 2, 6, 0.1));
    private final Setting.Num interval = add(new Setting.Num("Click Interval", 8, 2, 40, 1));
    private final Setting.Bool holdAttack = add(new Setting.Bool("Hold Attack", true));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    private int cd = 0;

    public SpearAura() {
        super("Spear Aura", "Auto-jabs nearby targets while holding a spear.", Category.SPEAR);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) cd--;
        if (cd > 0) return;
        if (!Hotbar.isSpear(player.getMainHandItem())) return;
        if (holdAttack.get() && !mc.options.keyAttack.isDown()) return;

        LivingEntity target = Aim.nearest(mc, player, reach.get(), playersOnly.get());
        if (target != null && Aim.silentClick(mc, player, target)) {
            cd = (int) interval.get();
        }
    }
}
