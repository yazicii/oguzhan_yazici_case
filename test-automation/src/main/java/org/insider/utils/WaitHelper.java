package org.insider.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.insider.config.ConfigManager;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Helper class for dynamic wait operations.
 * Timeout values are read from ConfigManager, can be overridden via mvn -Dtimeout.default=20.
 */
public class WaitHelper {

    private static final Logger log = LogManager.getLogger(WaitHelper.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait longWait;

    public WaitHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getDefaultTimeout()));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getShortTimeout()));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getLongTimeout()));
    }

    /**
     * Waits until the element is visible.
     */
    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is visible (WebElement overload for @FindBy).
     */
    public WebElement waitForElementVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits until the element is visible (with short timeout).
     */
    public WebElement waitForElementVisibleShort(By locator) {
        return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is visible (with long timeout).
     */
    public WebElement waitForElementVisibleLong(By locator) {
        return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is present in the DOM (does not need to be visible).
     */
    public WebElement waitForElementPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits until the element is present/visible (WebElement overload for @FindBy).
     */
    public WebElement waitForElementPresent(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits until the element is clickable.
     */
    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the element is clickable (WebElement overload for @FindBy).
     */
    public WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits until at least one element is visible.
     */
    public List<WebElement> waitForElementsVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until at least one element is present in the DOM.
     */
    public List<WebElement> waitForElementsPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until the element becomes invisible.
     */
    public boolean waitForElementInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits until the page title contains the specified text.
     */
    public boolean waitForTitleContains(String title) {
        return wait.until(ExpectedConditions.titleContains(title));
    }

    /**
     * Waits until the URL contains the specified text.
     */
    public boolean waitForUrlContains(String url) {
        return wait.until(ExpectedConditions.urlContains(url));
    }

    /**
     * Waits until a custom condition is met.
     */
    public <T> T waitForCondition(Function<WebDriver, T> condition) {
        return wait.until(condition);
    }

    /**
     * Waits until a custom condition is met (with the specified timeout).
     */
    public <T> T waitForCondition(Function<WebDriver, T> condition, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(condition);
    }

    /**
     * Waits for the element to be visible and clickable, then clicks it.
     */
    public void waitAndClick(By locator) {
        WebElement element = waitForElementClickable(locator);
        element.click();
    }

    /**
     * Waits for the element to be visible and clickable, then clicks it (WebElement overload for @FindBy).
     */
    public void waitAndClick(WebElement element) {
        waitForElementClickable(element).click();
    }

    /**
     * Waits for the element to be visible and sends text to it.
     */
    public void waitAndSendKeys(By locator, String text) {
        WebElement element = waitForElementVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Checks whether at least one element exists (with wait).
     */
    public boolean isElementPresent(By locator) {
        try {
            waitForElementPresent(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether at least one element is visible (with wait).
     */
    public boolean isElementVisible(By locator) {
        try {
            waitForElementVisible(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether at least one element exists (without wait, immediately).
     */
    public boolean isElementPresentImmediate(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the element is displayed (WebElement overload for @FindBy).
     */
    public boolean isElementDisplayedImmediate(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the list has at least one displayed element.
     */
    public boolean isAnyElementDisplayedImmediate(List<WebElement> elements) {
        if (elements == null || elements.isEmpty()) return false;
        try {
            return elements.stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Attempts to dismiss the cookie popup (if present).
     * Uses a specific selector for the Insider One site cookie popup.
     */
    public void dismissCookiePopup() {
        try {
            // Specific selector for Insider One cookie popup - Accept All button
            By acceptAllButton = By.id("wt-cli-accept-all-btn");
            
            // Check and click the cookie button
            if (isElementPresentImmediate(acceptAllButton)) {
                WebElement cookieButton = driver.findElement(acceptAllButton);
                if (cookieButton.isDisplayed()) {
                    waitForElementClickable(acceptAllButton).click();
                    log.info("Cookie popup dismissed - Accept All clicked");
                    
                    // Wait for the cookie popup container to disappear
                    try {
                        By cookieContainer = By.cssSelector(".cli-modal-backdrop, .wt-cli-cookie-bar");
                        if (isElementPresentImmediate(cookieContainer)) {
                            waitForElementInvisible(cookieContainer);
                        }
                    } catch (Exception e) {
                        // Popup may have already disappeared
                    }
                }
            }
        } catch (Exception e) {
            // Continue silently if cookie popup is absent or cannot be dismissed
        }
    }
}
