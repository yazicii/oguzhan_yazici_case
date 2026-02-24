package org.insider.driver;

import org.insider.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates WebDriver based on browser type.
 * Reads browser and headless settings from ConfigManager.
 * Can be overridden from command line via mvn -Dbrowser=firefox -Dheadless=true.
 */
public final class WebDriverFactory {

    private WebDriverFactory() { }

    public static WebDriver createDriver() {
        String browser = ConfigManager.getBrowser().toLowerCase();
        boolean headless = ConfigManager.isHeadless();

        return switch (browser) {
            case "firefox" -> createFirefoxDriver(headless);
            default -> createChromeDriver(headless);
        };
    }

    private static WebDriver createChromeDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.cookies", 1);
        prefs.put("profile.cookie_controls_mode", 0);
        prefs.put("profile.default_content_settings.popups", 0);
        options.setExperimentalOption("prefs", prefs);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        return new FirefoxDriver(options);
    }
}
