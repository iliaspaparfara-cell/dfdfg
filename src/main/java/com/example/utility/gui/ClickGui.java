package com.example.utility.gui;

import com.example.utility.config.Config;
import com.example.utility.feature.Category;
import com.example.utility.feature.Feature;
import com.example.utility.feature.FeatureManager;
import com.example.utility.setting.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Single-window ClickGUI: sidebar with categories + General pages, header with search,
 * and rounded module cards with toggle switches. Left-click toggles, right-click (or the arrow) expands.
 */
public class ClickGui extends Screen {
    private enum Page { MODULES, THEME, CONFIGS }

    private record SideItem(String label, Category cat, Page page, int y) {}

    private interface RowVisitor {
        /** s == null: module card; otherwise a setting row. Return true to stop. */
        boolean visit(Feature f, Setting s, int x, int y, int w, int h);
    }

    private static final int[] ACCENTS = {0xFF3B82F6, 0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899, 0xFF22C55E, 0xFFF97316};
    private static final String[] ACCENT_NAMES = {"Blue", "Indigo", "Purple", "Pink", "Green", "Orange"};

    private static final int SW = 132, CARD_H = 34, SET_H = 18, ROW_GAP = 4;
    private static final int NAVY = 0xFF0B0E1F;
    private static final int C_WIN = 0xEB0A0D1E, C_SIDE = 0xF0070914;
    private static final int C_CARD = 0xC0141836, C_CARD_HOVER = 0xC01B2148, C_SET = 0xB00D1128;
    private static final int C_TEXT = 0xFFFFFFFF, C_DIM = 0xFF8D97B5, C_DIM2 = 0xFF5F6A8C;

    private static Category selectedCat = Category.COMBAT;
    private static Page page = Page.MODULES;

    private final Set<Feature> expanded = new HashSet<>();
    private EditBox search;
    private double scroll, maxScroll;
    private boolean enabledOnly;
    private Setting.Num sliding;
    private int slideX, slideW;
    private String status = "";

    private int wx, wy, winW, winH, cx, cw, listY, listH;

    public ClickGui() {
        super(Component.literal("Cookie Client"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    protected void init() {
        winW = Math.min(540, width - 20);
        winH = Math.min(300, height - 10);
        wx = (width - winW) / 2;
        wy = (height - winH) / 2;
        cx = wx + SW + 14;
        cw = winW - SW - 28;
        listY = wy + 62;
        listH = winH - 62 - 10;

        int bx = wx + winW - 14 - 150, by = wy + 14;
        search = new EditBox(font, bx + 20, by + 5, 126, 10, Component.literal("Search"));
        search.setBordered(false);
        search.setHint(Component.literal("Search modules"));
        search.setResponder(t -> scroll = 0);
        search.visible = page == Page.MODULES;
        addRenderableWidget(search);
    }

    // ------------------------------------------------------------------ colours

    private int accent() {
        return ACCENTS[Math.min(Config.theme, ACCENTS.length - 1)];
    }

    private static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static int alpha(int rgb, int a) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private int selTint() {
        return alpha(mix(accent(), NAVY, 0.72), 0xE0);
    }

    private int accentLight() {
        return mix(accent(), 0xFFFFFFFF, 0.35);
    }

    // ------------------------------------------------------------------ data

    private List<Feature> categoryFeatures() {
        return FeatureManager.all().stream().filter(f -> f.getCategory() == selectedCat).toList();
    }

    private List<Feature> visibleFeatures() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase();
        return categoryFeatures().stream()
                .filter(f -> q.isEmpty() || f.getName().toLowerCase().contains(q))
                .filter(f -> !enabledOnly || f.isEnabled())
                .toList();
    }

    private List<SideItem> sideItems() {
        List<SideItem> list = new ArrayList<>();
        int y = wy + 62;
        for (Category c : Category.values()) {
            list.add(new SideItem(c.label, c, Page.MODULES, y));
            y += 19;
        }
        y += 24;
        list.add(new SideItem("Theme", null, Page.THEME, y));
        y += 19;
        list.add(new SideItem("Configs", null, Page.CONFIGS, y));
        return list;
    }

    private int walkList(List<Feature> feats, RowVisitor v) {
        int start = listY - (int) scroll;
        int y = start;
        for (Feature f : feats) {
            if (v.visit(f, null, cx, y, cw, CARD_H)) return 0;
            y += CARD_H;
            if (expanded.contains(f)) {
                for (Setting s : f.getSettings()) {
                    if (v.visit(f, s, cx + 8, y, cw - 16, SET_H)) return 0;
                    y += SET_H;
                }
            }
            y += ROW_GAP;
        }
        return y - start;
    }

    // ------------------------------------------------------------------ render

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x50000000);
        rrect(g, wx, wy, winW, winH, 8, C_WIN);

        renderSidebar(g, mx, my);
        switch (page) {
            case MODULES -> renderModules(g, mx, my);
            case THEME -> renderTheme(g, mx, my);
            case CONFIGS -> renderConfigs(g, mx, my);
        }
        super.render(g, mx, my, delta); // draws the search box on top
    }

    private void renderSidebar(GuiGraphics g, int mx, int my) {
        rrect(g, wx + 6, wy + 6, SW - 8, winH - 12, 6, C_SIDE);

        drawLogo(g, wx + 24, wy + 24, 9);
        g.drawString(font, Component.literal("Cookie").withStyle(ChatFormatting.BOLD), wx + 40, wy + 15, C_TEXT, true);
        int cw0 = font.width(Component.literal("Cookie").withStyle(ChatFormatting.BOLD));
        g.drawString(font, Component.literal(" Client").withStyle(ChatFormatting.ITALIC), wx + 40 + cw0, wy + 15, accentLight(), true);
        g.drawString(font, "BETA RELEASE 1.0.0", wx + 40, wy + 27, C_DIM2, false);

        g.drawString(font, "MODULES", wx + 16, wy + 50, C_DIM2, false);

        List<SideItem> items = sideItems();
        for (SideItem it : items) {
            if (it.page() == Page.THEME) {
                g.fill(wx + 16, it.y() - 14, wx + SW - 8, it.y() - 13, 0x22FFFFFF);
                g.drawString(font, "GENERAL", wx + 16, it.y() - 10, C_DIM2, false);
            }
            boolean selected = page == it.page() && (it.cat() == null || it.cat() == selectedCat);
            boolean hover = mx >= wx + 10 && mx < wx + SW - 6 && my >= it.y() && my < it.y() + 17;
            if (selected) rrect(g, wx + 10, it.y(), SW - 18, 17, 5, selTint());
            else if (hover) rrect(g, wx + 10, it.y(), SW - 18, 17, 5, 0x22FFFFFF);

            g.fill(wx + 18, it.y() + 7, wx + 21, it.y() + 10, selected ? accentLight() : C_DIM2);
            g.drawString(font, it.label(), wx + 27, it.y() + 5, selected ? C_TEXT : C_DIM, false);

            if (it.cat() != null) {
                long n = FeatureManager.all().stream().filter(f -> f.getCategory() == it.cat()).count();
                String s = String.valueOf(n);
                g.drawString(font, s, wx + SW - 14 - font.width(s), it.y() + 5, C_DIM2, false);
            }
        }
    }

    private void renderHeader(GuiGraphics g, String title, String sub) {
        Component t1 = Component.literal(title).withStyle(ChatFormatting.ITALIC);
        g.drawString(font, t1, cx, wy + 14, accentLight(), true);
        g.drawString(font, Component.literal(" Modules").withStyle(ChatFormatting.BOLD), cx + font.width(t1), wy + 14, C_TEXT, true);
        g.drawString(font, sub, cx, wy + 27, C_DIM2, false);
    }

    private void renderModules(GuiGraphics g, int mx, int my) {
        List<Feature> all = categoryFeatures();
        long on = all.stream().filter(Feature::isEnabled).count();
        renderHeader(g, selectedCat.label, all.size() + " modules \u00b7 " + on + " enabled");

        // search box backdrop + magnifier
        int bx = wx + winW - 14 - 150, by = wy + 14;
        rrect(g, bx, by, 150, 18, 5, 0x30FFFFFF);
        ring(g, bx + 9, by + 8, 4, C_DIM);
        g.fill(bx + 12, by + 11, bx + 14, by + 13, C_DIM);
        g.fill(bx + 14, by + 13, bx + 15, by + 14, C_DIM);

        // "Enabled" filter chip
        rrect(g, cx, wy + 40, 58, 14, 7, enabledOnly ? selTint() : 0x30FFFFFF);
        g.drawString(font, "Enabled", cx + 10, wy + 43, enabledOnly ? C_TEXT : C_DIM, false);

        List<Feature> feats = visibleFeatures();
        if (feats.isEmpty()) {
            String msg = all.isEmpty() ? "No modules in this category yet." : "No modules match.";
            g.drawString(font, msg, cx + (cw - font.width(msg)) / 2, listY + 40, C_DIM2, false);
            maxScroll = 0;
            return;
        }

        g.enableScissor(cx - 2, listY, cx + cw + 2, listY + listH);
        int total = walkList(feats, (f, s, x, y, w, h) -> {
            boolean inList = my >= listY && my < listY + listH;
            boolean hover = inList && mx >= x && mx < x + w && my >= y && my < y + h;

            if (s == null) {
                boolean enabled = f.isEnabled();
                rrect(g, x, y, w, h, 6, enabled ? selTint() : hover ? C_CARD_HOVER : C_CARD);
                g.drawString(font, Component.literal(f.getName()).withStyle(ChatFormatting.BOLD), x + 12, y + 7, C_TEXT, false);
                g.drawString(font, font.plainSubstrByWidth(f.getDescription(), w - 80), x + 12, y + 19, C_DIM2, false);
                if (!f.getSettings().isEmpty()) {
                    g.drawString(font, expanded.contains(f) ? "v" : ">", x + w - 58, y + 13, C_DIM, false);
                }
                drawSwitch(g, x + w - 40, y + 11, enabled);
            } else if (s instanceof Setting.Bool b) {
                g.fill(x, y, x + w, y + h, C_SET);
                g.drawString(font, b.getName(), x + 8, y + 5, C_DIM, false);
                drawSwitch(g, x + w - 34, y + 3, b.get());
            } else if (s instanceof Setting.Num n) {
                g.fill(x, y, x + w, y + h, C_SET);
                g.drawString(font, n.getName(), x + 8, y + 5, C_DIM, false);
                int tx = x + w - 150, tw = 100;
                double pct = (n.get() - n.getMin()) / (n.getMax() - n.getMin());
                rrect(g, tx, y + 7, tw, 4, 2, 0xFF262C4A);
                rrect(g, tx, y + 7, Math.max(4, (int) (tw * pct)), 4, 2, accent());
                String val = String.format("%.1f", n.get());
                g.drawString(font, val, x + w - 8 - font.width(val), y + 5, accentLight(), false);
            }
            return false;
        });
        g.disableScissor();

        maxScroll = Math.max(0, total - listH);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
    }

    private void renderTheme(GuiGraphics g, int mx, int my) {
        renderHeader(g, "Theme", "Pick an accent colour");
        for (int i = 0; i < ACCENTS.length; i++) {
            int[] r = swatchRect(i);
            if (i == Config.theme) rrect(g, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4, 7, 0xFFFFFFFF);
            rrect(g, r[0], r[1], r[2], r[3], 6, ACCENTS[i]);
            g.drawString(font, ACCENT_NAMES[i], r[0] + (r[2] - font.width(ACCENT_NAMES[i])) / 2, r[1] + r[3] + 6, C_DIM, false);
        }
    }

    private int[] swatchRect(int i) {
        int w = 96, h = 40, gap = 14;
        int col = i % 3, row = i / 3;
        return new int[]{cx + 4 + col * (w + gap), listY + 4 + row * (h + 30), w, h};
    }

    private void renderConfigs(GuiGraphics g, int mx, int my) {
        renderHeader(g, "Configs", "Save and load module settings");
        button(g, cx, listY, 110, 24, "Save config", mx, my);
        button(g, cx + 122, listY, 110, 24, "Load config", mx, my);
        if (!status.isEmpty()) g.drawString(font, status, cx, listY + 36, accentLight(), false);
        g.drawString(font, font.plainSubstrByWidth(Config.path().toString(), cw), cx, listY + 52, C_DIM2, false);
        g.drawString(font, "Auto-saved when you close this menu.", cx, listY + 66, C_DIM2, false);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        rrect(g, x, y, w, h, 6, hover ? selTint() : C_CARD);
        g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, C_TEXT, false);
    }

    // ------------------------------------------------------------------ input

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();

        // sidebar
        for (SideItem it : sideItems()) {
            if (in(mx, my, wx + 10, it.y(), SW - 18, 17)) {
                page = it.page();
                if (it.cat() != null) selectedCat = it.cat();
                scroll = 0;
                search.visible = page == Page.MODULES;
                search.setFocused(false);
                return true;
            }
        }

        if (page == Page.MODULES) {
            if (in(mx, my, wx + winW - 14 - 150, wy + 14, 150, 18)) {
                return super.mouseClicked(event, doubleClick); // focus the search box
            }
            search.setFocused(false);

            if (in(mx, my, cx, wy + 40, 58, 14)) {
                enabledOnly = !enabledOnly;
                scroll = 0;
                return true;
            }
            if (my >= listY && my < listY + listH) {
                walkList(visibleFeatures(), (f, s, x, y, w, h) -> {
                    if (!in(mx, my, x, y, w, h)) return false;
                    if (s == null) {
                        boolean arrow = !f.getSettings().isEmpty() && mx >= x + w - 70 && mx < x + w - 46;
                        if (button == 1 || arrow) {
                            if (!expanded.remove(f)) expanded.add(f);
                        } else if (button == 0) {
                            f.toggle();
                        }
                    } else if (button == 0) {
                        if (s instanceof Setting.Bool b) {
                            b.toggle();
                        } else if (s instanceof Setting.Num n && mx >= x + w - 160) {
                            sliding = n;
                            slideX = x + w - 150;
                            slideW = 100;
                            slide(mx);
                        }
                    }
                    return true;
                });
                return true; // click was inside the list area; consume it
            }
        } else if (page == Page.THEME) {
            for (int i = 0; i < ACCENTS.length; i++) {
                int[] r = swatchRect(i);
                if (in(mx, my, r[0], r[1], r[2], r[3])) {
                    Config.theme = i;
                    return true;
                }
            }
        } else if (page == Page.CONFIGS) {
            if (in(mx, my, cx, listY, 110, 24)) {
                status = Config.save();
                return true;
            }
            if (in(mx, my, cx + 122, listY, 110, 24)) {
                status = Config.load(true);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (sliding != null) {
            slide(event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        sliding = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (page == Page.MODULES && in(mouseX, mouseY, cx, listY, cw, listH)) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - scrollY * 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void slide(double mx) {
        double pct = Math.max(0, Math.min(1, (mx - slideX) / slideW));
        sliding.set(sliding.getMin() + pct * (sliding.getMax() - sliding.getMin()));
    }

    // ------------------------------------------------------------------ drawing helpers

    private void drawSwitch(GuiGraphics g, int x, int y, boolean on) {
        rrect(g, x, y, 26, 12, 6, on ? accent() : 0xFF262C4A);
        disc(g, on ? x + 19 : x + 7, y + 6, 4, C_TEXT);
    }

    /** Filled rounded rectangle (disjoint pieces, so translucent colours blend correctly). */
    private static void rrect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int dy = r - i;
            int inset = r - (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            g.fill(x + inset, y + i, x + r, y + i + 1, color);
            g.fill(x + w - r, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - 1 - i, x + r, y + h - i, color);
            g.fill(x + w - r, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    private static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void ring(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int outer = (int) Math.round(Math.sqrt(r * r - dy * dy));
            if (Math.abs(dy) >= r - 1) {
                g.fill(cx - outer, cy + dy, cx + outer + 1, cy + dy + 1, color);
            } else {
                int inner = (int) Math.round(Math.sqrt((r - 1) * (r - 1) - dy * dy));
                g.fill(cx - outer, cy + dy, cx - inner, cy + dy + 1, color);
                g.fill(cx + inner + 1, cy + dy, cx + outer + 1, cy + dy + 1, color);
            }
        }
    }

    /** Cookie body with a real bite cut out of the top right (no background colour needed). */
    private static void discCut(GuiGraphics g, int cx, int cy, int r, int color, int bx, int by, int br) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(r * r - dy * dy));
            int y = cy + dy;
            int l = cx - dx, rr = cx + dx + 1;
            int d = y - by;
            if (Math.abs(d) <= br) {
                int bd = (int) Math.round(Math.sqrt(br * br - d * d));
                int cl = bx - bd, cr = bx + bd + 1;
                if (cl > l) g.fill(l, y, Math.min(cl, rr), y + 1, color);
                if (cr < rr) g.fill(Math.max(cr, l), y, rr, y + 1, color);
            } else {
                g.fill(l, y, rr, y + 1, color);
            }
        }
    }

    private void drawLogo(GuiGraphics g, int cx, int cy, int r) {
        int bx = cx + r - 1, by = cy - r + 2;
        discCut(g, cx, cy, r, mix(accent(), NAVY, 0.35), bx, by, 4);
        discCut(g, cx, cy, r - 1, accent(), bx, by, 4);
        int chip = mix(accent(), NAVY, 0.8);
        int[][] chips = {{-5, -3}, {1, -6}, {3, 0}, {-2, 3}, {-6, 2}, {4, 5}, {-1, -1}};
        for (int[] c : chips) {
            g.fill(cx + c[0], cy + c[1], cx + c[0] + 2, cy + c[1] + 2, chip);
        }
    }
}
