package org.insider.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApiConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ApiConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private ApiConfig() { }

    public static String get(String key, String defaultValue) {
        String sysVal = System.getProperty(key);
        if (sysVal != null) return sysVal;
        return props.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getBaseUrl() {
        return get("api.base.url", "https://petstore.swagger.io/v2");
    }

    public static String getLogLevel() {
        return get("log.level", "INFO");
    }

    public static boolean isLogRequests() {
        return getBoolean("api.log.requests", false);
    }
}
