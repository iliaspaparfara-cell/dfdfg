package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

/**
 * Hold right-click while holding a mace: throws a wind charge straight down (silently),
 * launching you up for a mace smash, then returns to the mace.
 */
public class WindChargeMacro extends Feature {
    private final Setting.Num cooldown = add(new Setting.Num("Cooldown", 12, 2, 40, 1));
    private final Setting.Bool needMace = add(new Setting.Bool("Hold Mace", true));

    private int cd = 0;

    public WindChargeMacro() {
        super("Wind Charge Macro", "Right-click with a mace to wind-charge launch.");
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) cd--;
        if (cd > 0 || !mc.options.keyUse.isDown()) return;
        if (needMace.get() && !player.getMainHandItem().is(Items.MACE)) return;

        int slot = Hotbar.find(player, s -> s.is(Items.WIND_CHARGE));
        if (slot < 0) return;

        int previous = Hotbar.selected(player);
        Hotbar.select(player, slot);
        Aim.withRotation(player, player.getYRot(), 90f,
                () -> mc.gameMode.useItem(player, InteractionHand.MAIN_HAND));
        Hotbar.select(player, previous);

        cd = (int) cooldown.get();
    }
}
