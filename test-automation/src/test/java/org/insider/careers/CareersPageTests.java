package org.insider.careers;

import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.BaseTest;
import org.insider.data.CareersDataProvider;
import org.insider.data.JobCardInfo;
import org.insider.pages.careers.JobListingsPage;
import org.insider.pages.careers.LeverJobPage;
import org.insider.pages.careers.QualityAssurancePage;
import org.testng.Assert;
import org.testng.annotations.Test;

@Feature("Careers")
public class CareersPageTests extends BaseTest {

    private static final Logger log = LogManager.getLogger(CareersPageTests.class);

    @Test(dataProvider = "qaJobFilters", dataProviderClass = CareersDataProvider.class)
    @Story("QA Job Filtering")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Filters QA jobs by location, then verifies all results match the criteria (QA department is pre-selected).")
    public void filterQAJobsByLocationAndDepartmentAndCheckPresence(String location) {
        QualityAssurancePage qaPage = new QualityAssurancePage(getDriver());
        JobListingsPage jobListingsPage = new JobListingsPage(getDriver());

        Allure.step("Navigate to QA careers page", qaPage::navigateToQAPage);
        Allure.step("Click 'See all QA jobs'", qaPage::clickViewAllQAJobs);
        Allure.step("Filter jobs by location: " + location,
                () -> jobListingsPage.filterJobs(location));

        int jobCount = Allure.step("Get job count", jobListingsPage::getJobListingsCount);
        Allure.step("Assert: at least one job listing present",
                () -> Assert.assertTrue(jobCount > 0,
                        "Expected at least one job listing, found: " + jobCount));

        boolean allMatch = Allure.step("Verify all listings match criteria",
                jobListingsPage::verifyAllJobsMatchCriteria);
        String summary = jobListingsPage.getLastVerificationSummary();
        Allure.step("Assert: all listings match Position/Department/Location",
                () -> Assert.assertTrue(allMatch, summary));

        log.info("Verified {} job listing(s) — all match filter criteria.", jobCount);
    }

    @Test(dataProvider = "qaJobFilters", dataProviderClass = CareersDataProvider.class)
    @Story("View Role Redirect")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Captures first job card info, clicks 'View Role', and verifies Lever page details match.")
    public void viewRoleRedirectsToLeverApplicationPage(String location) {
        QualityAssurancePage qaPage = new QualityAssurancePage(getDriver());
        JobListingsPage jobListingsPage = new JobListingsPage(getDriver());

        Allure.step("Navigate to QA careers page", qaPage::navigateToQAPage);
        Allure.step("Click 'See all QA jobs'", qaPage::clickViewAllQAJobs);
        Allure.step("Filter jobs by location: " + location,
                () -> jobListingsPage.filterJobs(location));

        Allure.step("Assert: job listings must be available",
                () -> Assert.assertTrue(jobListingsPage.areJobListingsAvailable(),
                        "Job listings must be present to click View Role"));

        JobCardInfo cardInfo = Allure.step("Capture first job card info before click",
                jobListingsPage::getFirstJobCardInfo);

        Allure.step("Click 'View Role' on first listing", jobListingsPage::clickFirstViewRoleButton);

        boolean onLever = Allure.step("Verify redirect to Lever page",
                jobListingsPage::isOnLeverApplicationPage);
        Allure.step("Assert: URL should contain 'lever.co'",
                () -> Assert.assertTrue(onLever,
                        "Expected redirect to lever.co application page"));

        LeverJobPage leverPage = new LeverJobPage(getDriver());
        LeverJobPage.LeverVerificationResult result = Allure.step("Verify Lever page details match card",
                () -> leverPage.verifyJobDetailsMatch(cardInfo));
        Allure.step("Assert: job details must match",
                () -> Assert.assertTrue(result.success(),
                        "Lever page details mismatch: " + result.summary()));

        log.info("View Role redirect verified; card and Lever page details match.");
    }
}
