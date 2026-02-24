package org.insider.listeners;

import io.qameta.allure.Allure;
import org.insider.listeners.AllureLogAppender;
import org.insider.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * TestNG listener: Takes a screenshot and adds it to the Allure report when a test fails;
 * adds log output to the report at the end of each test (success or failure).
 * The listener must be registered to run before AllureTestNg so that attachments
 * can still be linked to the "current test".
 */
public class AllureFailureListener implements ITestListener, ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        AllureLogAppender.install();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            WebDriver driver = ((BaseTest) instance).getDriver();
            if (driver != null) {
                String name = result.getMethod().getMethodName();
                attachScreenshot(name, driver);
            }
        }
        attachTestLog();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachTestLog();
    }

    private void attachTestLog() {
        String logContent = AllureLogAppender.getAndClearForCurrentThread();
        if (logContent != null && !logContent.isBlank()) {
            Allure.addAttachment("Test log", "text/plain",
                    new ByteArrayInputStream(logContent.getBytes(StandardCharsets.UTF_8)), "txt");
        }
    }

    private void attachScreenshot(String testMethodName, WebDriver driver) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Screenshot (failure): " + testMethodName,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );
        } catch (Exception e) {
            Allure.addAttachment("Screenshot error", "text/plain", e.getMessage());
        }
    }
}
