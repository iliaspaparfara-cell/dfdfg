package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;

/**
 * Tells the server you are on the ground while falling so fall damage never builds up.
 * Skipped while you carry a mace by default, because resetting fall distance cancels the mace smash bonus.
 */
public class NoFall extends Feature {
    private final Setting.Num minFall = add(new Setting.Num("Min Fall", 1.0, 0.5, 2.5, 0.5));
    private final Setting.Bool skipWithMace = add(new Setting.Bool("Skip With Mace", true));

    public NoFall() {
        super("No Fall", "Cancels fall damage.", Category.MOVEMENT);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        if (p.onGround() || p.getAbilities().flying || p.isFallFlying() || p.isPassenger() || p.isInWater()) return;
        if (p.fallDistance < minFall.get()) return;
        if (skipWithMace.get() && Hotbar.find(p, s -> s.is(Items.MACE)) >= 0) return;

        p.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, p.horizontalCollision));
    }
}
