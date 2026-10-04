package com.example.utility.feature;

import com.example.utility.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/** Keeps a totem of undying in your off hand, taking it from anywhere in your inventory. */
public class AutoTotem extends Feature {
    private final Setting.Num delay = add(new Setting.Num("Delay", 2, 0, 10, 1));
    private final Setting.Bool hotbarFirst = add(new Setting.Bool("Hotbar First", true));

    private int cd = 0;

    public AutoTotem() {
        super("Auto Totem", "Moves a totem from your inventory into your off hand.", Category.MISC);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        if (cd > 0) {
            cd--;
            return;
        }
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        int index = findTotem(player);
        if (index < 0) return;

        // Inventory index -> player menu slot: hotbar 0-8 is menu 36-44, main 9-35 is menu 9-35.
        int menuSlot = index < 9 ? 36 + index : index;

        // Button 40 on a SWAP click swaps the clicked slot with the off hand.
        mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, menuSlot, 40, ClickType.SWAP, player);

        cd = (int) delay.get();
    }

    private int findTotem(LocalPlayer player) {
        // Search the preferred area first, then the other one.
        int[][] order = hotbarFirst.get()
                ? new int[][]{{0, 9}, {9, 36}}
                : new int[][]{{9, 36}, {0, 9}};
        for (int[] range : order) {
            for (int i = range[0]; i < range[1]; i++) {
                if (player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        return -1;
    }
}
