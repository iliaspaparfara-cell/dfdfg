package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import com.example.utility.util.Interact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;

/**
 * Speed bridging: hold your movement keys (e.g. walk backwards off an edge) and it sneaks at the edge
 * so you don't fall, while placing a block onto the next tile every tick.
 */
public class SpeedBridge extends Feature {
    private final Setting.Bool edgeSneak = add(new Setting.Bool("Edge Sneak", true));
    private final Setting.Num delay = add(new Setting.Num("Delay", 0, 0, 5, 1));

    private boolean forcedSneak = false;
    private int cd = 0;

    public SpeedBridge() {
        super("Speed Bridge", "Fast bridging: edge sneak + instant block placing.", Category.MOVEMENT);
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
        if (p == null || mc.level == null || mc.gameMode == null || mc.screen != null) {
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

        // Sneak when standing on the edge with nothing in front.
        BlockPos under = BlockPos.containing(p.getX(), p.getY() - 1.0, p.getZ());
        BlockPos ahead = BlockPos.containing(p.getX() + dx * 0.35, p.getY() - 1.0, p.getZ() + dz * 0.35);
        boolean onSolid = !mc.level.getBlockState(under).isAir();
        if (edgeSneak.get() && p.onGround() && onSolid && mc.level.getBlockState(ahead).isAir()) {
            mc.options.keyShift.setDown(true);
            forcedSneak = true;
        } else {
            releaseSneak(mc);
        }

        // Place onto the next tile(s).
        if (cd > 0) {
            cd--;
            return;
        }
        int slot = Hotbar.find(p, s -> !s.isEmpty() && s.getItem() instanceof BlockItem);
        if (slot < 0) return;
        int held = Hotbar.selected(p);
        if (p.getInventory().getItem(held).getItem() instanceof BlockItem) slot = held;

        for (double off : new double[]{0.35, 1.0}) {
            BlockPos target = BlockPos.containing(p.getX() + dx * off, p.getY() - 1.0, p.getZ() + dz * off);
            if (Interact.placeBlock(mc, p, slot, target)) {
                cd = (int) delay.get();
                return;
            }
        }
    }
}
