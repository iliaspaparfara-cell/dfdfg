package com.example.utility.gui;

import com.example.utility.config.Config;

public final class Theme {
    private Theme() {}

    public static final int[] ACCENTS = {0xFF8B5CF6, 0xFF3B82F6, 0xFF6366F1, 0xFFEC4899, 0xFF22C55E, 0xFFF97316};
    public static final String[] NAMES = {"Purple", "Blue", "Indigo", "Pink", "Green", "Orange"};

    public static int accent() {
        return ACCENTS[Math.max(0, Math.min(Config.theme, ACCENTS.length - 1))];
    }
}
