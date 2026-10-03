package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import com.example.utility.util.Interact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

/**
 * Places end crystals on obsidian/bedrock next to the nearest player and breaks crystals
 * near them. Skips crystals/spots that are too close to you and stops at low health.
 */
public class CrystalAura extends Feature {
    private final Setting.Num targetRange = add(new Setting.Num("Target Range", 8, 3, 12, 0.5));
    private final Setting.Num placeRange = add(new Setting.Num("Place Range", 4.5, 3, 5, 0.1));
    private final Setting.Num breakRange = add(new Setting.Num("Break Range", 3.0, 2, 5, 0.1));
    private final Setting.Num minSelfDist = add(new Setting.Num("Min Self Distance", 3.0, 1, 6, 0.5));
    private final Setting.Num minHealth = add(new Setting.Num("Min Health", 10, 1, 20, 1));
    private final Setting.Num placeDelay = add(new Setting.Num("Place Delay", 2, 0, 20, 1));
    private final Setting.Num breakDelay = add(new Setting.Num("Break Delay", 1, 0, 20, 1));
    private final Setting.Bool doPlace = add(new Setting.Bool("Place", true));
    private final Setting.Bool doBreak = add(new Setting.Bool("Break", true));

    private int placeCd = 0;
    private int breakCd = 0;

    public CrystalAura() {
        super("Crystal Aura", "Places and breaks end crystals around enemies.", Category.COMBAT);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (placeCd > 0) placeCd--;
        if (breakCd > 0) breakCd--;
        if (player.getHealth() <= minHealth.get()) return;

        LivingEntity target = Aim.nearest(mc, player, targetRange.get(), true);
        if (target == null) return;

        // Break first.
        if (doBreak.get() && breakCd == 0) {
            EndCrystal crystal = bestCrystal(mc, player, target);
            if (crystal != null && Aim.silentClick(mc, player, crystal)) {
                breakCd = (int) breakDelay.get();
                return;
            }
        }

        if (doPlace.get() && placeCd == 0) {
            int slot = Hotbar.find(player, s -> s.is(Items.END_CRYSTAL));
            if (slot < 0) return;
            BlockPos spot = bestSpot(mc, player, target);
            if (spot != null && Interact.useOn(mc, player, slot, spot, Direction.UP)) {
                placeCd = (int) placeDelay.get();
            }
        }
    }

    private EndCrystal bestCrystal(Minecraft mc, LocalPlayer player, LivingEntity target) {
        double br = breakRange.get();
        AABB box = player.getBoundingBox().inflate(br + 1);
        return mc.level.getEntitiesOfClass(EndCrystal.class, box, c ->
                        c.isAlive()
                                && Aim.distSqr(player, c) <= br * br
                                && player.getEyePosition().distanceTo(c.position()) >= minSelfDist.get()
                                && c.position().distanceTo(target.position()) <= 6.0)
                .stream()
                .min(Comparator.comparingDouble(c -> c.position().distanceTo(target.position())))
                .orElse(null);
    }

    /** Obsidian/bedrock block with two free blocks above, close to the target and far enough from you. */
    private BlockPos bestSpot(Minecraft mc, LocalPlayer player, LivingEntity target) {
        BlockPos center = target.blockPosition();
        Vec3 eye = player.getEyePosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    if (!st.is(Blocks.OBSIDIAN) && !st.is(Blocks.BEDROCK)) continue;
                    if (!mc.level.getBlockState(pos.above()).isAir()) continue;
                    if (!mc.level.getBlockState(pos.above(2)).isAir()) continue;

                    Vec3 crystalPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                    if (eye.distanceTo(Interact.faceCenter(pos, Direction.UP)) > placeRange.get()) continue;
                    if (eye.distanceTo(crystalPos) < minSelfDist.get()) continue;

                    AABB space = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(),
                            pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
                    if (!mc.level.getEntitiesOfClass(Entity.class, space).isEmpty()) continue;

                    double score = crystalPos.distanceTo(target.position());
                    if (score < bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }
}
