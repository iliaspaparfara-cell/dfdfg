package com.example.utility.setting;

public abstract class Setting {
    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class Bool extends Setting {
        private boolean value;

        public Bool(String name, boolean def) {
            super(name);
            this.value = def;
        }

        public boolean get() {
            return value;
        }

        public void toggle() {
            value = !value;
        }
    }

    public static class Num extends Setting {
        private double value;
        private final double min, max, step;

        public Num(String name, double def, double min, double max, double step) {
            super(name);
            this.min = min;
            this.max = max;
            this.step = step;
            set(def);
        }

        public double get() {
            return value;
        }

        public void set(double v) {
            double snapped = Math.round((v - min) / step) * step + min;
            value = Math.max(min, Math.min(max, snapped));
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }
}
