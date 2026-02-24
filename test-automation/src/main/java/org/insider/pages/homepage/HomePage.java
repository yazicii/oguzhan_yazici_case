package org.insider.pages.homepage;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.config.ConfigManager;
import org.insider.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Page Object for the Insider home page.
 * Verifies page load and presence of main content sections.
 * Section class names sourced from actual DOM inspection (homepage-* pattern).
 */
public class HomePage extends BasePage {

    private static final Logger log = LogManager.getLogger(HomePage.class);

    private static final String HOME_PAGE_URL = ConfigManager.getBaseUrl() + "/";
    private static final String EXPECTED_DOMAIN = ConfigManager.getExpectedDomain();

    @FindBy(css = "section.homepage-hero")
    private WebElement heroSection;

    @FindBy(css = "section.homepage-social-proof")
    private WebElement socialProofSection;

    @FindBy(css = "section.homepage-core-differentiators")
    private WebElement coreDifferentiatorsSection;

    @FindBy(css = "section.homepage-capabilities")
    private WebElement capabilitiesSection;

    @FindBy(css = "section.homepage-insider-one-ai")
    private WebElement siriusAISection;

    @FindBy(css = "section.homepage-channels")
    private WebElement channelsSection;

    @FindBy(css = "section.homepage-case-study")
    private WebElement caseStudiesSection;

    @FindBy(css = "section.homepage-analyst")
    private WebElement analystSection;

    @FindBy(css = "section.homepage-integrations")
    private WebElement integrationsSection;

    @FindBy(css = "section.homepage-resources")
    private WebElement resourcesSection;

    @FindBy(css = "section.homepage-call-to-action")
    private WebElement callToActionSection;

    private List<SectionEntry> getSections() {
        return List.of(
                new SectionEntry("Hero", heroSection),
                new SectionEntry("Social Proof", socialProofSection),
                new SectionEntry("Core Differentiators", coreDifferentiatorsSection),
                new SectionEntry("Capabilities", capabilitiesSection),
                new SectionEntry("Sirius AI", siriusAISection),
                new SectionEntry("Channels", channelsSection),
                new SectionEntry("Case Studies", caseStudiesSection),
                new SectionEntry("Analyst", analystSection),
                new SectionEntry("Integrations", integrationsSection),
                new SectionEntry("Resources", resourcesSection),
                new SectionEntry("Call to Action", callToActionSection)
        );
    }

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Navigate to home page")
    public void navigateToHomePage() {
        driver.get(HOME_PAGE_URL);
        waitHelper.dismissCookiePopup();
        waitHelper.waitForElementVisible(heroSection);
        log.info("Home page loaded: {}", HOME_PAGE_URL);
    }

    @Step("Verify home page opened (URL + title)")
    public boolean verifyHomePageOpened() {
        waitHelper.waitForUrlContains(EXPECTED_DOMAIN);

        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();

        boolean urlOk = currentUrl != null && currentUrl.contains(EXPECTED_DOMAIN);
        boolean titleOk = pageTitle != null && pageTitle.toLowerCase().contains("insider");

        if (!urlOk) log.warn("URL does not contain expected domain '{}'. Current: {}", EXPECTED_DOMAIN, currentUrl);
        if (!titleOk) log.warn("Title does not contain 'Insider'. Current: {}", pageTitle);

        return urlOk && titleOk;
    }

    @Step("Verify all main sections loaded")
    public boolean verifyMainBlocksLoaded() {
        int passed = 0;
        int total = getSections().size();

        log.info("=== Verifying {} main sections ===", total);

        for (SectionEntry entry : getSections()) {
            String name = entry.name;
            WebElement el = entry.element;
            try {
                if (el != null && waitHelper.isElementDisplayedImmediate(el)) {
                    passed++;
                    log.info("[{}] PASS — element found", name);
                } else {
                    log.warn("[{}] FAIL — element not found or not visible", name);
                }
            } catch (Exception e) {
                log.warn("[{}] FAIL — {}", name, e.getMessage());
            }
        }

        log.info("=== Section result: {}/{} passed ===", passed, total);

        if (passed < total) {
            List<String> failed = getSections().stream()
                    .filter(e -> {
                        try {
                            return e.element == null || !e.element.isDisplayed();
                        } catch (Exception ex) {
                            return true;
                        }
                    })
                    .map(e -> e.name)
                    .toList();
            log.warn("Failed sections: {}", failed);
        }

        return passed == total;
    }

    private record SectionEntry(String name, WebElement element) {}
}
