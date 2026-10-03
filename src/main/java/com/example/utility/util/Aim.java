package com.example.utility.util;

import com.example.utility.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.function.Predicate;

public final class Aim {
    private Aim() {}

    public static boolean isValid(LocalPlayer self, LivingEntity e, boolean playersOnly) {
        if (e == self || !e.isAlive() || e.isSpectator() || e.isInvulnerable()) return false;
        return !playersOnly || e instanceof Player;
    }

    /** Squared distance from the player's eyes to the closest point of the target's hitbox. */
    public static double distSqr(LocalPlayer self, Entity e) {
        Vec3 eye = self.getEyePosition();
        AABB bb = e.getBoundingBox();
        double x = Mth.clamp(eye.x, bb.minX, bb.maxX);
        double y = Mth.clamp(eye.y, bb.minY, bb.maxY);
        double z = Mth.clamp(eye.z, bb.minZ, bb.maxZ);
        return eye.distanceToSqr(x, y, z);
    }

    public static LivingEntity nearest(Minecraft mc, LocalPlayer self, double range, boolean playersOnly) {
        return nearest(mc, self, range, playersOnly, e -> true);
    }

    public static LivingEntity nearest(Minecraft mc, LocalPlayer self, double range, boolean playersOnly,
                                       Predicate<LivingEntity> extra) {
        AABB search = self.getBoundingBox().inflate(range + 1.0);
        return mc.level.getEntitiesOfClass(LivingEntity.class, search, e -> isValid(self, e, playersOnly) && extra.test(e))
                .stream()
                .filter(e -> distSqr(self, e) <= range * range)
                .min(Comparator.comparingDouble(e -> distSqr(self, e)))
                .orElse(null);
    }

    /** Yaw/pitch from the player's eyes to the middle of the target's hitbox. */
    public static float[] rotationTo(LocalPlayer self, Entity target) {
        return rotationToPoint(self, target.getBoundingBox().getCenter());
    }

    public static float[] rotationToPoint(LocalPlayer self, Vec3 t) {
        Vec3 eye = self.getEyePosition();
        double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f)};
    }

    /** Performs a genuine left click through the game's own input path (no hand-built attack packet). */
    public static boolean click(Minecraft mc) {
        return ((MinecraftAccessor) mc).utility$startAttack();
    }

    /**
     * Silent aim click: the server sees you look at the target and the click happens on it,
     * while your own camera never moves. Rotation is restored (and re-sent) right after.
     */
    public static boolean silentClick(Minecraft mc, LocalPlayer p, Entity target) {
        if (!p.hasLineOfSight(target)) return false;

        float oldYaw = p.getYRot(), oldPitch = p.getXRot();
        HitResult oldHit = mc.hitResult;
        float[] rot = rotationTo(p, target);

        sendRot(p, rot[0], rot[1]);
        p.setYRot(rot[0]);
        p.setXRot(rot[1]);
        mc.hitResult = new EntityHitResult(target);

        boolean clicked;
        try {
            clicked = click(mc);
        } finally {
            p.setYRot(oldYaw);
            p.setXRot(oldPitch);
            mc.hitResult = oldHit;
            sendRot(p, oldYaw, oldPitch);
        }
        return clicked;
    }

    /** Runs an action while the server sees the given rotation; your camera is restored right after. */
    public static void withRotation(LocalPlayer p, float yaw, float pitch, Runnable action) {
        float oldYaw = p.getYRot(), oldPitch = p.getXRot();
        sendRot(p, yaw, pitch);
        p.setYRot(yaw);
        p.setXRot(pitch);
        try {
            action.run();
        } finally {
            p.setYRot(oldYaw);
            p.setXRot(oldPitch);
            sendRot(p, oldYaw, oldPitch);
        }
    }

    private static void sendRot(LocalPlayer p, float yaw, float pitch) {
        p.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision));
    }
}
