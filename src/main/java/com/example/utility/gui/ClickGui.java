package com.example.utility.gui;

import com.example.utility.feature.Feature;
import com.example.utility.feature.FeatureManager;
import com.example.utility.setting.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public class ClickGui extends Screen {
    private static final int X = 30, Y = 30, W = 170, ROW = 16, SROW = 14;
    private static final int C_HEADER = 0xFF11111B, C_ROW = 0xFF181825, C_HOVER = 0xFF2A2A3C;
    private static final int C_ON = 0xFF3B82F6, C_SET = 0xFF1E1E2E, C_FILL = 0xFF2563A8, C_TEXT = 0xFFFFFFFF;

    private final Set<Feature> expanded = new HashSet<>();
    private Setting.Num dragging;

    public ClickGui() {
        super(Component.literal("Utility"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);

        g.fill(X, Y, X + W, Y + ROW, C_HEADER);
        g.drawString(font, "Utility", X + 6, Y + 4, C_TEXT, true);

        int y = Y + ROW;
        for (Feature f : FeatureManager.all()) {
            boolean hover = inside(mx, my, X, y, W, ROW);
            g.fill(X, y, X + W, y + ROW, f.isEnabled() ? C_ON : hover ? C_HOVER : C_ROW);
            g.drawString(font, f.getName(), X + 6, y + 4, C_TEXT, true);
            g.drawString(font, expanded.contains(f) ? "-" : "+", X + W - 12, y + 4, C_TEXT, true);
            y += ROW;

            if (expanded.contains(f)) {
                for (Setting s : f.getSettings()) {
                    g.fill(X, y, X + W, y + SROW, C_SET);
                    if (s instanceof Setting.Bool b) {
                        if (b.get()) g.fill(X, y, X + 3, y + SROW, C_ON);
                        g.drawString(font, b.getName() + ": " + (b.get() ? "ON" : "OFF"), X + 8, y + 3, C_TEXT, true);
                    } else if (s instanceof Setting.Num n) {
                        double pct = (n.get() - n.getMin()) / (n.getMax() - n.getMin());
                        g.fill(X, y, X + (int) (W * pct), y + SROW, C_FILL);
                        g.drawString(font, String.format("%s: %.1f", n.getName(), n.get()), X + 8, y + 3, C_TEXT, true);
                    }
                    y += SROW;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();

        int y = Y + ROW;
        for (Feature f : FeatureManager.all()) {
            if (inside(mx, my, X, y, W, ROW)) {
                if (button == 0) f.toggle();
                else if (button == 1 && !expanded.remove(f)) expanded.add(f);
                return true;
            }
            y += ROW;

            if (expanded.contains(f)) {
                for (Setting s : f.getSettings()) {
                    if (inside(mx, my, X, y, W, SROW) && button == 0) {
                        if (s instanceof Setting.Bool b) {
                            b.toggle();
                        } else if (s instanceof Setting.Num n) {
                            dragging = n;
                            slide(mx);
                        }
                        return true;
                    }
                    y += SROW;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            slide(event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    private void slide(double mx) {
        double pct = Math.max(0, Math.min(1, (mx - X) / W));
        dragging.set(dragging.getMin() + pct * (dragging.getMax() - dragging.getMin()));
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
