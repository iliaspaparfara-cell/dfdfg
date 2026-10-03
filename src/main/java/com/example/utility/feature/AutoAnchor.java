package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import com.example.utility.util.Interact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Anchor bomb: place a respawn anchor next to the target, charge it with glowstone,
 * then detonate it with a non-glowstone item. Works outside the Nether only.
 * Needs a respawn anchor and glowstone in the hotbar.
 */
public class AutoAnchor extends Feature {
    private enum Phase { IDLE, CHARGE, EXPLODE }

    private final Setting.Num targetRange = add(new Setting.Num("Target Range", 6, 3, 10, 0.5));
    private final Setting.Num placeRange = add(new Setting.Num("Place Range", 4.5, 3, 5, 0.1));
    private final Setting.Num minSelfDist = add(new Setting.Num("Min Self Distance", 3.0, 1, 5, 0.5));
    private final Setting.Num minHealth = add(new Setting.Num("Min Health", 10, 1, 20, 1));
    private final Setting.Num delay = add(new Setting.Num("Step Delay", 1, 0, 10, 1));
    private final Setting.Num cooldown = add(new Setting.Num("Cooldown", 10, 0, 40, 1));

    private Phase phase = Phase.IDLE;
    private BlockPos anchorPos;
    private int wait = 0;

    public AutoAnchor() {
        super("Auto Anchor", "Places, charges and detonates a respawn anchor on enemies.", Category.COMBAT);
    }

    @Override
    protected void onDisable() {
        reset(0);
    }

    private void reset(int cd) {
        phase = Phase.IDLE;
        anchorPos = null;
        wait = cd;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (wait > 0) {
            wait--;
            return;
        }
        if (mc.level.dimension() == Level.NETHER) return; // anchors only explode outside the Nether
        if (player.getHealth() <= minHealth.get()) {
            reset(0);
            return;
        }

        switch (phase) {
            case IDLE -> {
                LivingEntity target = Aim.nearest(mc, player, targetRange.get(), true);
                if (target == null) return;
                int anchorSlot = Hotbar.find(player, s -> s.is(Items.RESPAWN_ANCHOR));
                int glowSlot = Hotbar.find(player, s -> s.is(Items.GLOWSTONE));
                if (anchorSlot < 0 || glowSlot < 0) return;

                BlockPos spot = findSpot(mc, player, target);
                if (spot == null) return;

                if (Interact.useOn(mc, player, anchorSlot, spot.below(), Direction.UP)) {
                    anchorPos = spot;
                    phase = Phase.CHARGE;
                    wait = (int) delay.get();
                }
            }
            case CHARGE -> {
                BlockState st = mc.level.getBlockState(anchorPos);
                if (!st.is(Blocks.RESPAWN_ANCHOR)) {
                    reset((int) cooldown.get());
                    return;
                }
                if (st.getValue(RespawnAnchorBlock.CHARGE) >= 1) {
                    phase = Phase.EXPLODE;
                    wait = (int) delay.get();
                    return;
                }
                int glowSlot = Hotbar.find(player, s -> s.is(Items.GLOWSTONE));
                if (glowSlot < 0) {
                    reset((int) cooldown.get());
                    return;
                }
                Interact.useOn(mc, player, glowSlot, anchorPos, Direction.UP);
                wait = (int) delay.get();
            }
            case EXPLODE -> {
                BlockState st = mc.level.getBlockState(anchorPos);
                if (st.is(Blocks.RESPAWN_ANCHOR)) {
                    // Anything but glowstone, otherwise the click just adds charge instead of exploding.
                    int slot = Hotbar.find(player, s -> !s.is(Items.GLOWSTONE) && !s.is(Items.RESPAWN_ANCHOR));
                    if (slot >= 0) Interact.useOn(mc, player, slot, anchorPos, Direction.UP);
                }
                reset((int) cooldown.get());
            }
        }
    }

    /** Free spot beside the target, as far from you as possible while still in reach. */
    private BlockPos findSpot(Minecraft mc, LocalPlayer player, LivingEntity target) {
        BlockPos base = target.blockPosition();
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        BlockPos best = null;
        double bestDist = -1;
        Vec3 eye = player.getEyePosition();

        for (int[] o : offsets) {
            BlockPos pos = base.offset(o[0], 0, o[1]);
            BlockState here = mc.level.getBlockState(pos);
            BlockState below = mc.level.getBlockState(pos.below());
            if (!here.isAir() || below.isAir() || !below.getFluidState().isEmpty()) continue;

            Vec3 top = Interact.faceCenter(pos.below(), Direction.UP);
            double d = eye.distanceTo(top);
            if (d > placeRange.get()) continue;
            if (eye.distanceTo(Vec3.atCenterOf(pos)) < minSelfDist.get()) continue;
            if (d > bestDist) {
                bestDist = d;
                best = pos;
            }
        }
        return best;
    }
}
