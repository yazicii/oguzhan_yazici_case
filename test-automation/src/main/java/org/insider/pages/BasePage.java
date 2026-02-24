package org.insider.pages;

import org.insider.utils.WaitHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * Base page that all Page Object classes extend.
 * Provides shared driver, WaitHelper, and PageFactory initialization.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WaitHelper waitHelper;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }
}
