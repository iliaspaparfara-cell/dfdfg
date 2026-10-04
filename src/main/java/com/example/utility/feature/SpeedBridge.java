package com.example.utility.feature;

import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;

/**
 * Edge sneak for bridging: when you are standing on the edge and your movement keys point off it,
 * sneak is held for you so you never fall. You place the blocks yourself.
 */
public class SpeedBridge extends Feature {
    private final Setting.Num edgeDistance = add(new Setting.Num("Edge Distance", 0.35, 0.1, 0.8, 0.05));
    private final Setting.Bool onlyWithBlocks = add(new Setting.Bool("Only With Blocks", true));

    private boolean forcedSneak = false;

    public SpeedBridge() {
        super("Speed Bridge", "Auto-sneaks at the edge so you can bridge fast.", Category.MOVEMENT);
    }

    @Override
    protected void onDisable() {
        releaseSneak(Minecraft.getInstance());
    }

    private void releaseSneak(Minecraft mc) {
        if (forcedSneak) mc.options.keyShift.setDown(false);
        forcedSneak = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.screen != null) {
            releaseSneak(mc);
            return;
        }

        if (onlyWithBlocks.get() && !(p.getMainHandItem().getItem() instanceof BlockItem)) {
            releaseSneak(mc);
            return;
        }

        // Direction you are steering, from the movement keys and where you look.
        double fwd = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double strafe = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
        if (fwd == 0 && strafe == 0) {
            releaseSneak(mc);
            return;
        }
        double yaw = Math.toRadians(p.getYRot());
        double dx = -Math.sin(yaw) * fwd + Math.cos(yaw) * strafe;
        double dz = Math.cos(yaw) * fwd + Math.sin(yaw) * strafe;
        double len = Math.sqrt(dx * dx + dz * dz);
        dx /= len;
        dz /= len;

        BlockPos under = BlockPos.containing(p.getX(), p.getY() - 1.0, p.getZ());
        BlockPos ahead = BlockPos.containing(
                p.getX() + dx * edgeDistance.get(), p.getY() - 1.0, p.getZ() + dz * edgeDistance.get());

        boolean onSolid = !mc.level.getBlockState(under).isAir();
        if (p.onGround() && onSolid && mc.level.getBlockState(ahead).isAir()) {
            mc.options.keyShift.setDown(true);
            forcedSneak = true;
        } else {
            releaseSneak(mc);
        }
    }
}
