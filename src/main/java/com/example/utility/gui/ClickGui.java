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
    private static final int X = 30, Y = 30, W = 170, ROW = 16, SROW = 14, HEADER = 30;
    private static final int C_HEADER = 0xFF11111B, C_ROW = 0xFF181825, C_HOVER = 0xFF2A2A3C;
    private static final int C_LOGO = 0xFF3B82F6, C_LOGO_EDGE = 0xFF1D4ED8, C_CHIP = 0xFF0B2A6B, C_TITLE = 0xFF93C5FD, C_SUB = 0xFF6B7FA8;
    private static final int C_ON = 0xFF3B82F6, C_SET = 0xFF1E1E2E, C_FILL = 0xFF2563A8, C_TEXT = 0xFFFFFFFF;

    private final Set<Feature> expanded = new HashSet<>();
    private Setting.Num dragging;

    public ClickGui() {
        super(Component.literal("Cookie Client"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);

        g.fill(X, Y, X + W, Y + HEADER, C_HEADER);
        g.fill(X, Y + HEADER - 2, X + W, Y + HEADER, C_ON);
        drawLogo(g, X + 17, Y + 14, 10);
        g.drawString(font, "Cookie Client", X + 34, Y + 6, C_TITLE, true);
        g.drawString(font, "Right Shift", X + 34, Y + 17, C_SUB, false);

        int y = Y + HEADER;
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

        int y = Y + HEADER;
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

    /** Blue cookie: round body, darker chips, and a bite out of the top right. */
    private void drawLogo(GuiGraphics g, int cx, int cy, int r) {
        disc(g, cx, cy, r, C_LOGO_EDGE);
        disc(g, cx, cy, r - 1, C_LOGO);
        int[][] chips = {{-5, -3}, {1, -6}, {3, 0}, {-2, 3}, {-6, 2}, {4, 5}, {-1, -1}};
        for (int[] c : chips) {
            g.fill(cx + c[0], cy + c[1], cx + c[0] + 2, cy + c[1] + 2, C_CHIP);
        }
        disc(g, cx + r - 1, cy - r + 2, 4, C_HEADER); // bite
    }

    private static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
