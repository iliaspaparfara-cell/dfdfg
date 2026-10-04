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
    private final Setting.Num range = add(new Setting.Num("Range", 64, 8, 128, 4));
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players Only", true));

    private Set<Integer> glowing = new HashSet<>();

    public Esp() {
        super("ESP", "Glow outline on entities, visible through walls.", Category.VISUAL);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (int id : glowing) {
                Entity e = mc.level.getEntity(id);
                if (e != null) e.setGlowingTag(false);
            }
        }
        glowing = new HashSet<>();
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        Set<Integer> now = new HashSet<>();
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range.get()),
                en -> en != player && en.isAlive() && (!playersOnly.get() || en instanceof Player))) {
            e.setGlowingTag(true);
            now.add(e.getId());
        }
        for (int id : glowing) {
            if (now.contains(id)) continue;
            Entity e = mc.level.getEntity(id);
            if (e != null) e.setGlowingTag(false);
        }
        glowing = now;
    }
}
