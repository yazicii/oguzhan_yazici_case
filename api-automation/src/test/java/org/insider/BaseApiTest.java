package org.insider;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.insider.config.ApiConfig;
import org.testng.annotations.BeforeClass;

import io.qameta.allure.restassured.AllureRestAssured;

public abstract class BaseApiTest {

    protected RequestSpecification spec;

    /** Generates a unique pet ID for test isolation (reduces collision risk). */
    protected long uniquePetId() {
        return 90_000_000L + Math.abs(System.nanoTime() % 10_000_000L);
    }

    @BeforeClass(alwaysRun = true)
    public void setupSpec() {
        RestAssured.baseURI = ApiConfig.getBaseUrl();

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured());
        if (ApiConfig.isLogRequests()) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL))
                   .addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }
        spec = builder.build();
    }
}
