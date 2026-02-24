package org.insider.pages.careers;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.data.JobCardInfo;
import org.insider.config.ConfigManager;
import org.insider.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the Lever job application detail page (jobs.lever.co).
 * Used to verify job details match the card clicked on the listings page.
 */
public class LeverJobPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LeverJobPage.class);

    @FindBy(css = "div.posting-headline h2")
    private WebElement postingHeadline;

    @FindBy(css = "div.posting-categories div.location")
    private WebElement categoryLocation;

    @FindBy(css = "div.posting-categories div.department")
    private WebElement categoryDepartment;

    public LeverJobPage(WebDriver driver) {
        super(driver);
    }

    @Step("Wait for Lever job page to load")
    public void waitForPageLoad() {
        waitHelper.waitForCondition(d -> d.getCurrentUrl() != null && d.getCurrentUrl().contains("lever.co"),
                ConfigManager.getDefaultTimeout());
        waitHelper.waitForElementVisible(postingHeadline);
    }

    @Step("Get job title from Lever page")
    public String getJobTitle() {
        return getText(postingHeadline);
    }

    @Step("Get location from Lever page")
    public String getLocation() {
        return getText(categoryLocation);
    }

    @Step("Get department from Lever page")
    public String getDepartment() {
        return getText(categoryDepartment);
    }

    /**
     * Verifies h2 (title), department, and location on Lever page match the card info.
     */
    @Step("Verify Lever page (h2, department, location) match card: {expected}")
    public LeverVerificationResult verifyJobDetailsMatch(JobCardInfo expected) {
        waitForPageLoad();

        String title = getJobTitle();
        String location = getLocation();
        String department = getDepartment();

        log.info("Lever page: h2='{}', location='{}', department='{}'", title, location, department);

        List<String> mismatches = new ArrayList<>();
        if (!expected.titleMatches(title)) {
            mismatches.add(String.format("h2/title: expected contains '%s', got '%s'", expected.title(), title));
        }
        if (!expected.locationMatches(location)) {
            mismatches.add(String.format("location: expected contains '%s', got '%s'", expected.location(), location));
        }
        if (!expected.departmentMatches(department)) {
            mismatches.add(String.format("department: expected contains '%s', got '%s'", expected.department(), department));
        }

        boolean success = mismatches.isEmpty();
        String summary = success
                ? "h2, department, location match."
                : "Mismatches: " + String.join("; ", mismatches);

        return new LeverVerificationResult(success, summary, title, location, department);
    }

    private String getText(WebElement element) {
        try {
            return element != null ? element.getText() : "";
        } catch (Exception e) {
            log.debug("Element not found: {}", e.getMessage());
            return "";
        }
    }

    public record LeverVerificationResult(
            boolean success,
            String summary,
            String title,
            String location,
            String department
    ) {}
}
