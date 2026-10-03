package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Hold attack with a mace and an enemy a few blocks away: instantly swaps to a Lunge spear,
 * jabs toward the enemy (silently turned), and swaps back to the mace.
 */
public class InstantLunge extends Feature {
    private final Setting.Num maxRange = add(new Setting.Num("Max Range", 10, 4, 16, 0.5));
    private final Setting.Num minRange = add(new Setting.Num("Min Range", 3.5, 2, 8, 0.5));
    private final Setting.Num cooldown = add(new Setting.Num("Cooldown", 20, 4, 60, 1));
    private final Setting.Bool lungeOnly = add(new Setting.Bool("Lunge Spear Only", true));
    private final Setting.Bool airOnly = add(new Setting.Bool("Air Only", false));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    private int cd = 0;

    public InstantLunge() {
        super("Instant Lunge", "Hold attack with a mace to lunge at a far enemy.");
    }

    private static boolean isSpear(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("_spear");
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) cd--;
        if (cd > 0 || !mc.options.keyAttack.isDown()) return;
        if (!player.getMainHandItem().is(Items.MACE)) return;
        if (airOnly.get() && player.onGround()) return;

        int spear = Hotbar.find(player,
                s -> isSpear(s) && (!lungeOnly.get() || Hotbar.hasEnchantNamed(s, "lunge")));
        if (spear < 0) return;

        LivingEntity target = Aim.nearest(mc, player, maxRange.get(), playersOnly.get());
        if (target == null) return;
        if (Aim.distSqr(player, target) <= minRange.get() * minRange.get()) return;
        if (!player.hasLineOfSight(target)) return;

        float[] rot = Aim.rotationTo(player, target);
        int previous = Hotbar.selected(player);

        Hotbar.select(player, spear);
        Aim.withRotation(player, rot[0], rot[1], () -> Aim.click(mc));
        Hotbar.select(player, previous);

        cd = (int) cooldown.get();
    }
}
