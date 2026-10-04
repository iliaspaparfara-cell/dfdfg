package com.example.utility.feature;

import com.example.utility.setting.Setting;
import com.example.utility.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.Items;

/** Keeps a totem of undying in your off hand, taking it from the hotbar. */
public class AutoTotem extends Feature {
    private final Setting.Num delay = add(new Setting.Num("Delay", 2, 0, 10, 1));

    private int cd = 0;

    public AutoTotem() {
        super("Auto Totem", "Moves a hotbar totem into your off hand.", Category.MISC);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (cd > 0) {
            cd--;
            return;
        }
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        int slot = Hotbar.find(player, s -> s.is(Items.TOTEM_OF_UNDYING));
        if (slot < 0) return;

        int previous = Hotbar.selected(player);
        if (slot != previous) player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        if (slot != previous) player.connection.send(new ServerboundSetCarriedItemPacket(previous));

        cd = (int) delay.get();
    }
}
