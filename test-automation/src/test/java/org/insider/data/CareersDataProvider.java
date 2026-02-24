package org.insider.data;

import org.testng.annotations.DataProvider;

/**
 * Provides test data for careers page tests.
 * Only location is used for filtering; QA department is pre-selected on the page.
 * Add new rows to test additional locations (e.g. "Istanbul, Turkiye").
 */
public class CareersDataProvider {

    @DataProvider(name = "qaJobFilters")
    public static Object[][] qaJobFilters() {
        return new Object[][] {
                { "Istanbul, Turkiye" }
        };
    }
}
