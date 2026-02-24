package org.insider.pages.careers;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.config.ConfigManager;
import org.insider.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page Object for the Quality Assurance careers page.
 * Navigates to QA page and clicks "See all QA jobs" with proper wait conditions.
 */
public class QualityAssurancePage extends BasePage {

    private static final Logger log = LogManager.getLogger(QualityAssurancePage.class);

    private static final String QA_PAGE_URL = ConfigManager.getBaseUrl() + ConfigManager.getCareersPath();

    @FindBy(css = "a[href*='open-positions'][href*='qualityassurance']")
    private WebElement viewAllQAJobs;

    public QualityAssurancePage(WebDriver driver) {
        super(driver);
    }

    @Step("Navigate to Quality Assurance careers page")
    public void navigateToQAPage() {
        driver.get(QA_PAGE_URL);
        waitHelper.waitForUrlContains("quality-assurance");
        waitHelper.dismissCookiePopup();
        log.info("QA careers page loaded: {}", QA_PAGE_URL);
    }

    private static final By JOBS_LIST = By.cssSelector("#jobs-list");
    private static final By JOB_ITEMS = By.cssSelector("#jobs-list > div.position-list-item");
    private static final By NO_POSITIONS = By.cssSelector("#jobs-list .no-job-result");

    @Step("Click 'See all QA jobs' and wait for job listings to load")
    public void clickViewAllQAJobs() {
        waitHelper.waitAndClick(viewAllQAJobs);
        waitHelper.waitForUrlContains("open-positions");
        waitHelper.waitForElementVisible(JOBS_LIST);

        waitHelper.waitForCondition(d -> {
            boolean hasNoPositions = d.findElements(NO_POSITIONS).stream().anyMatch(WebElement::isDisplayed);
            if (hasNoPositions) return true;
            return !d.findElements(JOB_ITEMS).isEmpty();
        }, 25);

        log.info("Job listings page loaded");
    }
}
