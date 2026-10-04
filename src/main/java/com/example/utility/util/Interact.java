package com.example.utility.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class Interact {
    private Interact() {}

    public static Vec3 faceCenter(BlockPos pos, Direction face) {
        return Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
    }

    /**
     * Right-clicks a block face using the item in the given hotbar slot (silently turned toward it),
     * then returns to the slot you had selected.
     */
    public static boolean useOn(Minecraft mc, LocalPlayer p, int slot, BlockPos pos, Direction face) {
        Vec3 hit = faceCenter(pos, face);
        float[] rot = Aim.rotationToPoint(p, hit);
        int previous = Hotbar.selected(p);
        boolean[] ok = new boolean[1];

        Hotbar.select(p, slot);
        Aim.withRotation(p, rot[0], rot[1], () -> {
            InteractionResult r = mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND,
                    new BlockHitResult(hit, face, pos, false));
            ok[0] = r.consumesAction();
        });
        if (ok[0]) p.swing(InteractionHand.MAIN_HAND);
        Hotbar.select(p, previous);
        return ok[0];
    }

    private static final Direction[] ORDER = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP};

    /** Places the item in the slot into the given empty block position, clicking any solid neighbour. */
    public static boolean placeBlock(Minecraft mc, LocalPlayer p, int slot, BlockPos target) {
        BlockState st = mc.level.getBlockState(target);
        if (!st.isAir() && !st.canBeReplaced()) return false;

        for (Direction dir : ORDER) {
            BlockPos neighbor = target.relative(dir);
            BlockState ns = mc.level.getBlockState(neighbor);
            if (ns.isAir() || ns.canBeReplaced() || !ns.getFluidState().isEmpty()) continue;
            return useOn(mc, p, slot, neighbor, dir.getOpposite());
        }
        return false;
    }
}
