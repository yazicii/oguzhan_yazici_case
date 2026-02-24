package org.insider.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration manager.
 * Values are read first from system properties, otherwise from config.properties.
 * This allows command-line override via mvn -Dbrowser=firefox -Denv=staging.
 *
 * Environment-based URL resolution:
 *   env=production → uses env.production.base.url
 *   env=staging    → uses env.staging.base.url
 *   env=test       → uses env.test.base.url
 */
public final class ConfigManager {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private ConfigManager() { }

    public static String get(String key) {
        String sysVal = System.getProperty(key);
        return sysVal != null ? sysVal : props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }

    // ── Browser ──
    public static String getBrowser() { return get("browser", "chrome"); }
    public static boolean isHeadless() { return getBoolean("headless", false); }

    // ── Environment ──
    public static String getEnv() { return get("env", "production"); }

    /**
     * Returns the base URL for the active environment.
     * First looks up env.{env}.base.url, otherwise uses base.url as fallback.
     */
    public static String getBaseUrl() {
        String env = getEnv();
        String envUrl = get("env." + env + ".base.url");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        return get("base.url", "https://useinsider.com");
    }

    /**
     * Returns the careers path for the active environment.
     */
    public static String getCareersPath() {
        String env = getEnv();
        return get("env." + env + ".careers.path", "/careers/quality-assurance/");
    }

    /**
     * Returns the domain expected to appear in the browser after redirect.
     * E.g.: when useinsider.com redirects to insiderone.com, returns "insiderone.com".
     * Uses env.{env}.expected.domain if defined, otherwise derives from base.url.
     */
    public static String getExpectedDomain() {
        String env = getEnv();
        String domain = get("env." + env + ".expected.domain");
        if (domain != null && !domain.isBlank()) {
            return domain;
        }
        return getBaseUrl().replaceFirst("https?://", "").replaceFirst("/.*", "");
    }

    // ── Timeout ──
    public static int getDefaultTimeout() { return getInt("timeout.default", 15); }
    public static int getShortTimeout() { return getInt("timeout.short", 5); }
    public static int getLongTimeout() { return getInt("timeout.long", 30); }

    // ── Allure ──
    public static boolean isSkipOpenAllure() { return getBoolean("skip.open.allure", false); }

    // ── Logging ──
    public static String getLogLevel() { return get("log.level", "INFO"); }
}
