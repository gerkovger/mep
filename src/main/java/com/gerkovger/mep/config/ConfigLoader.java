package com.gerkovger.mep.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigLoader {

    private static final Path configFilePath = Path.of(System.getProperty("user.home"), ".config", "mep", "mep.conf");

    private final Properties properties = new Properties();

    public ConfigLoader() {
        try {
            if (Files.exists(configFilePath))
                properties.load(new FileInputStream(configFilePath.toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        var iS = getString(key);
        try {
            return Integer.parseInt(iS);
        } catch (NumberFormatException e) {
            System.out.printf("Not an int or not set '%s'. Loading default value: %d%n", key, defaultValue);
        }
        return defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        var iS = getString(key);
        try {
            return Float.parseFloat(iS);
        } catch (NumberFormatException e) {
            System.out.printf("Not a float or not set '%s'. Loading default value: %f%n", key, defaultValue);
        }
        return defaultValue;
    }

    public List<String> getStringList(String key, List<String> defaultValue) {
        var s = getString(key);
        if (s == null || s.isBlank()) return defaultValue;
        return Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toList());
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        var s = getString(key);
        if (s == null || s.isBlank()) return defaultValue;
        return Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        var bS = getString(key);
        return bS == null ? defaultValue : Boolean.parseBoolean(bS);
    }

}
