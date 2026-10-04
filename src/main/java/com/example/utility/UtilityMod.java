package com.example.utility;

import com.example.utility.config.Config;
import com.example.utility.feature.FeatureManager;
import com.example.utility.gui.ClickGui;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class UtilityMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FeatureManager.init();
        Config.load(false); // restore saved settings (modules stay off until you enable them)

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("utilitymod", "hud"),
                (graphics, delta) -> FeatureManager.renderHud(graphics, Minecraft.getInstance()));

        KeyMapping openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Open Utility GUI", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.consumeClick()) {
                mc.setScreen(new ClickGui());
            }
            FeatureManager.tick(mc);
        });
    }
}
