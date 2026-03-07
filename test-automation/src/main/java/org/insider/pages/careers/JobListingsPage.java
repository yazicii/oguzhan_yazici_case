package org.insider.pages.careers;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.config.ConfigManager;
import org.insider.data.JobCardInfo;
import org.insider.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Page Object for the careers open-positions page.
 * Handles filter dropdowns, job listing verification, and View Role navigation.
 * Uses @FindBy for stable-state element access and By constants inside
 * wait conditions / lambdas where fresh DOM lookups are required.
 */
public class JobListingsPage extends BasePage {

    private static final Logger log = LogManager.getLogger(JobListingsPage.class);

    // ── @FindBy fields (stable-state access after page is loaded) ──

    @FindBy(css = "#filter-by-location")
    private WebElement locationFilter;

    @FindBy(css = "#filter-by-department")
    private WebElement departmentFilter;

    @FindBy(css = "#jobs-list")
    private WebElement jobsListContainer;

    // ── By constants (used in wait conditions / lambdas) ──

    private static final By JOBS_LIST_BY = By.cssSelector("#jobs-list");
    private static final By JOB_ITEMS_BY = By.cssSelector("#jobs-list > div.position-list-item");
    private static final By NO_POSITIONS_BY = By.cssSelector("#jobs-list .no-job-result");
    private static final By LOCATION_FILTER_BY = By.cssSelector("#filter-by-location");
    private static final By DEPARTMENT_FILTER_BY = By.cssSelector("#filter-by-department");
    private static final By FILTER_BTN_BY = By.cssSelector("button[type='submit'], button[class*='filter'], button[class*='apply']");
    private static final By CARD_TITLE_BY = By.cssSelector(".position-title, h3, h4, a[href*='lever.co']");
    private static final By CARD_DEPARTMENT_BY = By.cssSelector("[class*='department'], [class*='team']");
    private static final By CARD_LOCATION_BY = By.cssSelector("[class*='location']");

    private static final int FILTER_WAIT_SECONDS = ConfigManager.getLongTimeout();
    private static final int CARD_EXCERPT_LENGTH = 150;

    private String lastVerificationSummary = "";

    public JobListingsPage(WebDriver driver) {
        super(driver);
    }

    // ═══════════════════════════════════════════════════════════
    // Filtering
    // ═══════════════════════════════════════════════════════════

    @Step("Filter jobs by location: {location}")
    public void filterJobs(String location) {
        waitForFiltersReady();

        List<WebElement> elementsBefore = new ArrayList<>(driver.findElements(JOB_ITEMS_BY));
        log.info("Jobs before filter: {}", elementsBefore.size());

        selectLocation(location);
        applyFilters();
        waitForJobListRefresh(elementsBefore);

        log.info("Jobs after filter: {}", driver.findElements(JOB_ITEMS_BY).size());
    }

    private void waitForFiltersReady() {
        waitHelper.waitForElementVisible(JOBS_LIST_BY);

        try {
            waitHelper.waitForCondition(d -> {
                try {
                    int locOpts = new Select(d.findElement(LOCATION_FILTER_BY)).getOptions().size();
                    int deptOpts = new Select(d.findElement(DEPARTMENT_FILTER_BY)).getOptions().size();
                    return locOpts > 1 && deptOpts >= 1;
                } catch (Exception e) {
                    return false;
                }
            }, FILTER_WAIT_SECONDS);
        } catch (org.openqa.selenium.TimeoutException e) {
            logFilterState();
            throw e;
        }

        log.info("Filter dropdowns ready");
    }

    private void selectLocation(String location) {
        waitHelper.waitForElementVisible(LOCATION_FILTER_BY);
        Select locationSelect = new Select(locationFilter);

        String current = locationSelect.getFirstSelectedOption().getText();
        if (current.equalsIgnoreCase(location)) {
            log.info("Location already set to: {}", current);
            return;
        }

        try {
            locationSelect.selectByVisibleText(location);
            log.info("Location selected: {}", location);
            return;
        } catch (Exception e) {
            log.debug("Exact text match failed for '{}', trying normalized: {}", location, e.getMessage());
        }

        String normalized = location.toLowerCase().replaceAll("[\\s,\\-]+", "");
        for (WebElement option : locationSelect.getOptions()) {
            String optNorm = option.getText().toLowerCase().replaceAll("[\\s,\\-]+", "");
            if (optNorm.equals(normalized)) {
                option.click();
                log.info("Location selected (normalized): {}", option.getText());
                return;
            }
        }

        throw new IllegalArgumentException("Location option not found: " + location);
    }

    private void applyFilters() {
        try {
            List<WebElement> btns = driver.findElements(FILTER_BTN_BY);
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) {
                    btn.click();
                    return;
                }
            }
        } catch (Exception e) {
            log.debug("No filter button found or click failed, filter may auto-apply");
        }
    }

    private void waitForJobListRefresh(List<WebElement> elementsBefore) {
        int countBefore = elementsBefore.size();

        waitHelper.waitForCondition(d -> {
            if (isNoPositionsVisible(d)) return true;

            if (countBefore == 0) {
                return !d.findElements(JOB_ITEMS_BY).isEmpty();
            }

            int currentCount = d.findElements(JOB_ITEMS_BY).size();
            if (currentCount != countBefore) return true;

            try {
                elementsBefore.getFirst().isDisplayed();
            } catch (StaleElementReferenceException e) {
                return true;
            }

            return false;
        }, FILTER_WAIT_SECONDS);

        final int[] lastSeenCount = {-1};
        waitHelper.waitForCondition(d -> {
            if (isNoPositionsVisible(d)) return true;
            int count = d.findElements(JOB_ITEMS_BY).size();
            if (count > 0 && count == lastSeenCount[0]) return true;
            lastSeenCount[0] = count;
            return false;
        }, FILTER_WAIT_SECONDS);
    }

    // ═══════════════════════════════════════════════════════════
    // Job listing queries
    // ═══════════════════════════════════════════════════════════

    @Step("Check if jobs list container is present")
    public boolean isJobsListContainerPresent() {
        try {
            return jobsListContainer != null && jobsListContainer.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Check if job listings are available")
    public boolean areJobListingsAvailable() {
        if (!isJobsListContainerPresent()) return false;
        if (isNoPositionsVisible(driver)) return false;
        return getVisibleJobCount() > 0;
    }

    @Step("Get job listings count")
    public int getJobListingsCount() {
        if (isNoPositionsVisible(driver)) return 0;
        return getVisibleJobCount();
    }

    public List<WebElement> getJobListingElements() {
        if (isNoPositionsVisible(driver)) return List.of();
        return driver.findElements(JOB_ITEMS_BY);
    }

    private int getVisibleJobCount() {
        return (int) driver.findElements(JOB_ITEMS_BY).stream()
                .filter(WebElement::isDisplayed).count();
    }

    private boolean isNoPositionsVisible(WebDriver d) {
        try {
            return d.findElements(NO_POSITIONS_BY).stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Verification
    // ═══════════════════════════════════════════════════════════

    @Step("Verify all job listings match criteria (Position/Department/Location)")
    public boolean verifyAllJobsMatchCriteria(String expectedLocation) {
        try {
            List<String> cardTexts = collectCardTexts();
            if (cardTexts.isEmpty()) {
                lastVerificationSummary = "No visible job card text found to verify.";
                log.warn(lastVerificationSummary);
                return false;
            }

            int total = 0, matched = 0;
            int lastFailIndex = -1;
            String lastFailExcerpt = "";
            String lastFailReason = "";

            log.info("\n=== Verifying Job Listings (expected location: {}) ===", expectedLocation);

            for (int i = 0; i < cardTexts.size(); i++) {
                int cardNum = i + 1;
                String text = cardTexts.get(i);
                String lower = text.toLowerCase();
                String excerpt = text.length() > CARD_EXCERPT_LENGTH
                        ? text.substring(0, CARD_EXCERPT_LENGTH).trim() + "..." : text.trim();

                boolean hasQA = lower.contains("quality assurance") || lower.contains("qa");
                boolean locationMatches = locationMatchesFilter(lower, expectedLocation);

                if (!hasQA || !locationMatches) {
                    lastFailIndex = cardNum;
                    lastFailExcerpt = excerpt;
                    lastFailReason = String.format("Not a QA card for location '%s' (QA=%s, location=%s)", expectedLocation, hasQA, locationMatches);
                    log.debug("[Card #{}] Skipped: {}", cardNum, lastFailReason);
                    continue;
                }

                total++;
                boolean departmentOk = lower.contains("quality assurance");

                log.info("[Card #{}] {}", cardNum, excerpt);
                log.info("[Card #{}] Position={}, Department={}, Location={}", cardNum, hasQA, departmentOk, locationMatches);

                if (departmentOk) {
                    matched++;
                    log.info("[Card #{}] PASS", cardNum);
                } else {
                    lastFailIndex = cardNum;
                    lastFailExcerpt = excerpt;
                    lastFailReason = String.format("position=%s, department=%s, location=%s", hasQA, departmentOk, locationMatches);
                    log.warn("[Card #{}] FAIL: {}", cardNum, lastFailReason);
                }
            }

            log.info("=== Result: {}/{} matched ===", matched, total);

            boolean success = total > 0 && matched == total;
            if (success) {
                lastVerificationSummary = String.format("All %d QA cards for '%s' match criteria.", total, expectedLocation);
            } else if (lastFailIndex > 0) {
                lastVerificationSummary = String.format(
                        "Expected: all QA cards for '%s' to match. Found: %d total, %d matched, %d failed. "
                        + "Last failing card #%d: \"%s\" | %s",
                        expectedLocation, total, matched, total - matched, lastFailIndex,
                        lastFailExcerpt.replace("\"", "'"), lastFailReason);
            } else {
                lastVerificationSummary = String.format(
                        "No QA cards for '%s' found among %d card(s).", expectedLocation, cardTexts.size());
            }
            return success;
        } catch (Exception e) {
            lastVerificationSummary = "Verification error: " + e.getMessage();
            log.error("Error verifying job listings: {}", e.getMessage());
            return false;
        }
    }

    public String getLastVerificationSummary() {
        return lastVerificationSummary;
    }

    private boolean locationMatchesFilter(String cardTextLower, String expectedLocation) {
        if (expectedLocation == null || expectedLocation.isBlank()) return true;
        String[] tokens = expectedLocation.split("[,\\s]+");
        for (String token : tokens) {
            if (!token.isBlank() && cardTextLower.contains(token.toLowerCase())) return true;
        }
        return false;
    }

    private List<String> collectCardTexts() {
        List<String> texts = new ArrayList<>();
        for (WebElement job : getJobListingElements()) {
            try {
                if (job.isDisplayed()) {
                    String text = job.getText();
                    if (!text.isBlank()) texts.add(text);
                }
            } catch (StaleElementReferenceException e) {
                log.debug("Stale element skipped during text collection");
            }
        }
        return texts;
    }

    // ═══════════════════════════════════════════════════════════
    // View Role & Card Info
    // ═══════════════════════════════════════════════════════════

    @Step("Get first job card info (h2, location, department)")
    public JobCardInfo getFirstJobCardInfo() {
        WebElement firstCard = getJobListingElements().stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible job listing found"));

        String title = getTextFromCard(firstCard, CARD_TITLE_BY);
        String location = getTextFromCard(firstCard, CARD_LOCATION_BY);
        String department = getTextFromCard(firstCard, CARD_DEPARTMENT_BY);

        if (title.isBlank() || location.isBlank() || department.isBlank()) {
            String fullText = firstCard.getText();
            log.debug("Parsing card from full text ({} chars)", fullText.length());
            String[] lines = fullText.split("\\n");
            if (title.isBlank() && lines.length > 0) title = lines[0].trim();
            for (String line : lines) {
                String l = line.trim();
                if (location.isBlank() && (l.contains("Istanbul") || l.contains("Turkiye") || l.contains(","))) location = l;
                if (department.isBlank() && (l.toLowerCase().contains("quality") || l.toLowerCase().contains("assurance"))) department = l;
            }
        }

        JobCardInfo info = new JobCardInfo(title, location, department);
        log.info("Captured card: h2='{}', location='{}', department='{}'", info.title(), info.location(), info.department());
        return info;
    }

    private String getTextFromCard(WebElement card, By by) {
        try {
            List<WebElement> els = card.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed()) {
                    String t = el.getText();
                    if (!t.isBlank() && !t.equalsIgnoreCase("View Role")) return t.trim();
                }
            }
        } catch (Exception e) {
            log.debug("Could not get text for {}: {}", by, e.getMessage());
        }
        return "";
    }

    @Step("Click 'View Role' on first job listing")
    public void clickFirstViewRoleButton() {
        WebElement firstJob = getJobListingElements().stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible job listing found"));

        WebElement link = findViewRoleLink(firstJob);
        if (link == null) {
            link = findViewRoleLink(jobsListContainer);
        }
        if (link == null) {
            throw new IllegalStateException("View Role link not found in job listing");
        }

        String originalHandle = driver.getWindowHandle();
        link.click();

        waitHelper.waitForCondition(d -> d.getWindowHandles().size() > 1, ConfigManager.getDefaultTimeout());
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                log.info("Switched to new tab for Lever application");
                break;
            }
        }
    }

    @Step("Verify redirect to Lever application page")
    public boolean isOnLeverApplicationPage() {
        try {
            waitHelper.waitForCondition(d -> {
                String url = d.getCurrentUrl();
                return url != null && url.contains("lever.co");
            }, ConfigManager.getDefaultTimeout());
            String url = driver.getCurrentUrl();
            boolean onLever = url != null && url.contains("lever.co");
            if (onLever) log.info("On Lever application page: {}", url);
            return onLever;
        } catch (Exception e) {
            log.warn("Not on Lever application page: {}", e.getMessage());
            return false;
        }
    }

    // ── Helpers ──

    private WebElement findViewRoleLink(WebElement scope) {
        for (By by : new By[]{
                By.cssSelector("a[href*='lever.co']"),
                By.cssSelector("a.btn-navy"),
                By.linkText("View Role"),
                By.partialLinkText("View Role")
        }) {
            try {
                for (WebElement el : scope.findElements(by)) {
                    if (el.isDisplayed() && el.isEnabled()) return el;
                }
            } catch (Exception ignored) { }
        }
        return null;
    }

    private void logFilterState() {
        int locOpts = -1, deptOpts = -1;
        try { locOpts = new Select(driver.findElement(LOCATION_FILTER_BY)).getOptions().size(); } catch (Exception ignored) { }
        try { deptOpts = new Select(driver.findElement(DEPARTMENT_FILTER_BY)).getOptions().size(); } catch (Exception ignored) { }
        log.warn("waitForFiltersReady timed out — Location options={}, Department options={}", locOpts, deptOpts);
    }
}
