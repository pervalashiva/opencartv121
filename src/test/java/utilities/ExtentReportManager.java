package utilities;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import TestBase.BaseClass;

public class ExtentReportManager implements ITestListener {

	public ExtentSparkReporter sparkReporter;
	public static ExtentReports extent;
	public static ExtentTest test;

	String repName;

	public void onStart(ITestContext testContext) {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		repName = "Test-Report-" + timeStamp + ".html";

		File reportDirectory = new File(System.getProperty("user.dir"), "reports");
		if (!reportDirectory.exists()) {
			reportDirectory.mkdirs();
		}

		File reportFile = new File(reportDirectory, repName);
		sparkReporter = new ExtentSparkReporter(reportFile.getAbsolutePath());
		sparkReporter.config().setDocumentTitle("OpenCart Automation Report");
		sparkReporter.config().setReportName("OpenCart Functional Testing");
		sparkReporter.config().setTheme(Theme.DARK);

		extent = new ExtentReports();
		extent.attachReporter(sparkReporter);

		extent.setSystemInfo("Application", "OpenCart");
		extent.setSystemInfo("Module", "Admin");
		extent.setSystemInfo("SubModule", "Customers");
		extent.setSystemInfo("User Name", System.getProperty("user.name"));
		extent.setSystemInfo("Environment", "QA");

		String os = testContext.getCurrentXmlTest().getParameter("os");
		extent.setSystemInfo("Operating System", os);

		String browser = testContext.getCurrentXmlTest().getParameter("browser");
		extent.setSystemInfo("Browser", browser);

		List<String> includeGroups = testContext.getCurrentXmlTest().getIncludedGroups();
		if (!includeGroups.isEmpty()) {
			extent.setSystemInfo("Groups", includeGroups.toString());
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		test = extent.createTest(result.getTestClass().getName());
		test.assignCategory(result.getMethod().getGroups());
		test.log(Status.PASS, result.getName() + " passed successfully");
	}

	@Override
	public void onTestFailure(ITestResult result) {
		test = extent.createTest(result.getTestClass().getName());
		test.assignCategory(result.getMethod().getGroups());
		test.log(Status.FAIL, result.getName() + " failed");
		if (result.getThrowable() != null) {
			test.log(Status.FAIL, result.getThrowable());
		}

		try {
			String imgPath = BaseClass.captureScreenshot(result.getName());
			test.addScreenCaptureFromPath(imgPath);
		} catch (IOException e) {
			test.log(Status.WARNING, "Screenshot capture failed: " + e.getMessage());
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		test = extent.createTest(result.getTestClass().getName());
		test.assignCategory(result.getMethod().getGroups());
		test.log(Status.SKIP, result.getName() + " skipped");
		if (result.getThrowable() != null) {
			test.log(Status.INFO, result.getThrowable().getMessage());
		}

		try {
			String screenshotPath = BaseClass.captureScreenshot(result.getMethod().getMethodName());
			test.addScreenCaptureFromPath(screenshotPath);
		} catch (IOException e) {
			test.log(Status.WARNING, "Screenshot capture failed for skipped test: " + e.getMessage());
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		if (extent != null) {
			extent.flush();
		}

		File extentReport = new File(new File(System.getProperty("user.dir"), "reports"), repName);
		if (!extentReport.exists()) {
			System.out.println("Extent report file not found: " + extentReport.getAbsolutePath());
			return;
		}

		System.out.println("Extent report: " + extentReport.getAbsolutePath());

		// Never call Desktop APIs on CI / headless JVMs (throws HeadlessException)
		if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
			return;
		}
		try {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				desktop.browse(extentReport.toURI());
			}
		} catch (Exception e) {
			System.out.println("Skipping auto-open of Extent report: " + e.getMessage());
		}
	}
}
