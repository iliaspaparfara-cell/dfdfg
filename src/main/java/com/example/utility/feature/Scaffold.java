package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import com.example.utility.util.Interact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Places blocks from your hotbar under (and slightly ahead of) you as you move. */
public class Scaffold extends Feature {
    private static final Direction[] ORDER = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP};

    private final Setting.Num lookahead = add(new Setting.Num("Lookahead", 0.6, 0, 2, 0.1));
    private final Setting.Num delay = add(new Setting.Num("Delay", 0, 0, 5, 1));

    private int cd = 0;

    public Scaffold() {
        super("Scaffold", "Places blocks under you while you walk.", Category.MOVEMENT);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) {
            cd--;
            return;
        }

        int slot = Hotbar.find(p, s -> !s.isEmpty() && s.getItem() instanceof BlockItem);
        if (slot < 0) return;

        // Prefer the block you're already holding.
        int held = Hotbar.selected(p);
        if (p.getInventory().getItem(held).getItem() instanceof BlockItem) slot = held;

        Vec3 v = p.getDeltaMovement();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);
        Vec3 ahead = p.position();
        if (speed > 0.01 && lookahead.get() > 0) {
            ahead = ahead.add(v.x / speed * lookahead.get(), 0, v.z / speed * lookahead.get());
        }

        BlockPos[] candidates = {
                BlockPos.containing(ahead.x, p.getY() - 1.0, ahead.z),
                BlockPos.containing(p.getX(), p.getY() - 1.0, p.getZ())
        };

        for (BlockPos target : candidates) {
            BlockState st = mc.level.getBlockState(target);
            if (!st.isAir() && !st.canBeReplaced()) continue;

            for (Direction dir : ORDER) {
                BlockPos neighbor = target.relative(dir);
                BlockState ns = mc.level.getBlockState(neighbor);
                if (ns.isAir() || ns.canBeReplaced() || !ns.getFluidState().isEmpty()) continue;

                if (Interact.useOn(mc, p, slot, neighbor, dir.getOpposite())) {
                    cd = (int) delay.get();
                }
                return;
            }
        }
    }
}
