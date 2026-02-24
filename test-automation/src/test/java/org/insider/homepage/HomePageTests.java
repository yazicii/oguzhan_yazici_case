package org.insider.homepage;

import io.qameta.allure.*;
import org.insider.BaseTest;
import org.insider.pages.homepage.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

@Feature("Home Page")
public class HomePageTests extends BaseTest {

    @Test
    @Story("Page Load")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifies home page opens and all main sections (hero, trusted, features etc.) are loaded.")
    public void verifyHomePageOpensAndAllBlocksLoad() {
        HomePage homePage = new HomePage(getDriver());

        Allure.step("Navigate to home page", homePage::navigateToHomePage);

        boolean pageOpened = Allure.step("Verify home page opened (URL + title)",
                homePage::verifyHomePageOpened);
        Allure.step("Assert: home page should be open",
                () -> Assert.assertTrue(pageOpened, "Home page URL or title check failed."));

        boolean blocksLoaded = Allure.step("Verify all main sections loaded",
                homePage::verifyMainBlocksLoaded);
        Allure.step("Assert: all main sections should be loaded",
                () -> Assert.assertTrue(blocksLoaded, "One or more main sections failed to load."));
    }

}
