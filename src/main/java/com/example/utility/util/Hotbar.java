package com.example.utility.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Predicate;

public final class Hotbar {
    private Hotbar() {}

    /** First hotbar slot (0-8) whose stack matches, or -1. */
    public static int find(LocalPlayer p, Predicate<ItemStack> test) {
        for (int i = 0; i < 9; i++) {
            if (test.test(p.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    public static int selected(LocalPlayer p) {
        return p.getInventory().getSelectedSlot();
    }

    public static void select(LocalPlayer p, int slot) {
        if (slot >= 0 && slot <= 8) p.getInventory().setSelectedSlot(slot);
    }

    public static boolean hasBreach(ItemStack stack) {
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return false;
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (h.is(Enchantments.BREACH)) return true;
        }
        return false;
    }

    /** True if the stack carries an enchantment whose registry id ends with the given name (e.g. "lunge"). */
    public static boolean hasEnchantNamed(ItemStack stack, String name) {
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return false;
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (h.getRegisteredName().endsWith(name)) return true;
        }
        return false;
    }

    public static boolean isSpear(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_spear");
    }
}
