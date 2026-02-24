package org.insider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.config.ConfigManager;
import org.insider.driver.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Base class that all test classes extend.
 * Browser creation is delegated to WebDriverFactory, configuration to ConfigManager.
 * No implicit wait is used; all waits are explicit via WaitHelper.
 * In parallel execution, each thread uses its own driver (ThreadLocal).
 */
public abstract class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();

    /** In parallel execution, each thread gets its own driver. */
    public WebDriver getDriver() {
        return driverHolder.get();
    }

    @BeforeMethod
    public void setUp() {
        WebDriver d = WebDriverFactory.createDriver();
        driverHolder.set(d);
        int longTimeout = ConfigManager.getLongTimeout();
        d.manage().timeouts().implicitlyWait(Duration.ZERO);
        d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(longTimeout));
        d.manage().timeouts().scriptTimeout(Duration.ofSeconds(longTimeout));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver d = driverHolder.get();
        if (d != null) {
            try {
                d.quit();
            } catch (Exception e) {
                log.warn("Driver quit failed: {}", e.getMessage());
            } finally {
                driverHolder.remove();
            }
        }
    }
}
