package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Aim;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Throw an ender pearl normally. A moment later this fires a wind charge at the pearl
 * (silently aimed) so the blast catches it mid-air.
 */
public class AutoPearlCatch extends Feature {
    private final Setting.Num delay = add(new Setting.Num("Delay", 1, 0, 10, 1));
    private final Setting.Num catchRange = add(new Setting.Num("Catch Range", 8, 3, 16, 0.5));
    private final Setting.Bool lead = add(new Setting.Bool("Lead Pearl", true));

    private final Set<Integer> handled = new HashSet<>();
    private int pearlId = -1;
    private int wait = 0;

    public AutoPearlCatch() {
        super("Auto Pearl Catch", "Wind-charges your own ender pearl mid-air.", Category.MACE);
    }

    @Override
    protected void onDisable() {
        pearlId = -1;
        wait = 0;
        handled.clear();
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null) return;

        if (pearlId >= 0) {
            if (wait > 0) {
                wait--;
                return;
            }
            Entity pearl = mc.level.getEntity(pearlId);
            pearlId = -1;
            if (pearl != null && pearl.isAlive()) catchPearl(mc, player, pearl);
            return;
        }

        // Look for a freshly thrown pearl of ours.
        double r = catchRange.get();
        for (Entity e : mc.level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(r),
                en -> en.getType() == EntityType.ENDER_PEARL)) {
            if (handled.contains(e.getId())) continue;
            if (!(e instanceof Projectile proj) || proj.getOwner() != player) continue;
            handled.add(e.getId());
            if (handled.size() > 32) handled.clear();
            pearlId = e.getId();
            wait = (int) delay.get();
            return;
        }
    }

    private void catchPearl(Minecraft mc, LocalPlayer player, Entity pearl) {
        int slot = Hotbar.find(player, s -> s.is(Items.WIND_CHARGE));
        if (slot < 0) return;

        Vec3 point = pearl.getBoundingBox().getCenter();
        if (lead.get()) point = point.add(pearl.getDeltaMovement());
        if (player.getEyePosition().distanceTo(point) > catchRange.get()) return;

        float[] rot = Aim.rotationToPoint(player, point);
        int previous = Hotbar.selected(player);

        Hotbar.select(player, slot);
        Aim.withRotation(player, rot[0], rot[1],
                () -> mc.gameMode.useItem(player, InteractionHand.MAIN_HAND));
        Hotbar.select(player, previous);
    }
}
