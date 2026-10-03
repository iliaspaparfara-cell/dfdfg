package com.example.utility.config;

import com.example.utility.feature.Feature;
import com.example.utility.feature.FeatureManager;
import com.example.utility.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Saves/loads module states and settings to .minecraft/config/cookieclient.properties. */
public final class Config {
    private Config() {}

    /** Index of the selected accent colour in the Theme page. */
    public static int theme = 0;

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("cookieclient.properties");
    }

    private static String key(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    public static String save() {
        try {
            Properties p = new Properties();
            for (Feature f : FeatureManager.all()) {
                String k = key(f.getName());
                p.setProperty(k + ".enabled", String.valueOf(f.isEnabled()));
                for (Setting s : f.getSettings()) {
                    String sk = k + "." + key(s.getName());
                    if (s instanceof Setting.Bool b) p.setProperty(sk, String.valueOf(b.get()));
                    else if (s instanceof Setting.Num n) p.setProperty(sk, String.valueOf(n.get()));
                }
            }
            p.setProperty("theme", String.valueOf(theme));
            try (OutputStream out = Files.newOutputStream(path())) {
                p.store(out, "Cookie Client");
            }
            return "Config saved.";
        } catch (Exception e) {
            return "Save failed: " + e.getMessage();
        }
    }

    /** @param applyEnabled also restore which modules were switched on */
    public static String load(boolean applyEnabled) {
        Path file = path();
        if (!Files.exists(file)) return "No saved config yet.";
        try {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            }
            for (Feature f : FeatureManager.all()) {
                String k = key(f.getName());
                for (Setting s : f.getSettings()) {
                    String v = p.getProperty(k + "." + key(s.getName()));
                    if (v == null) continue;
                    if (s instanceof Setting.Bool b) b.set(Boolean.parseBoolean(v));
                    else if (s instanceof Setting.Num n) n.set(Double.parseDouble(v));
                }
                String en = p.getProperty(k + ".enabled");
                if (applyEnabled && en != null) f.setEnabled(Boolean.parseBoolean(en));
            }
            String t = p.getProperty("theme");
            if (t != null) theme = Math.max(0, Integer.parseInt(t));
            return "Config loaded.";
        } catch (Exception e) {
            return "Load failed: " + e.getMessage();
        }
    }
}
