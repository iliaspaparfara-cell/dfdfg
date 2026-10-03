package com.example.utility.feature;

public enum Category {
    COMBAT("Combat"),
    MACE("Mace"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    SPEAR("Spear"),
    VISUAL("Visual");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
