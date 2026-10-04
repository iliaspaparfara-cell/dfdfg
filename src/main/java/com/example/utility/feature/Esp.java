package com.example.utility.feature;

import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

/** Outlines players (or all living entities) through walls using the game's glow outline. */
public class Esp extends Feature {
    /** Entity ids the renderer should draw with the glow outline (read by EntityMixin). */
    private static volatile Set<Integer> targets = Set.of();

    public static boolean shouldGlow(Entity e) {
        return !targets.isEmpty() && targets.contains(e.getId());
    }

    private final Setting.Num range = add(new Setting.Num("Range", 64, 8, 128, 4));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    public Esp() {
        super("ESP", "Glow outline on entities, visible through walls.", Category.VISUAL);
    }

    @Override
    protected void onDisable() {
        targets = Set.of();
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            targets = Set.of();
            return;
        }

        Set<Integer> now = new HashSet<>();
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range.get()),
                en -> en != player && en.isAlive() && (!playersOnly.get() || en instanceof Player))) {
            now.add(e.getId());
        }
        targets = now;
    }
}
