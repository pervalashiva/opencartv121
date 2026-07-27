package testCases;

import org.testng.Assert;
import org.testng.annotations.Test;

import TestBase.BaseClass;
import pageObjects.HomePage;
import pageObjects.LoginPage;
import pageObjects.MyAccountPage;
import utilities.DataProviders;

/*
 *  Data is valid - login success-test pass -logout
 *               login failed - test fail
 *
 *  Data is invalid - login success - test fail - logout
 *                 login failed - test pass
 */

public class TC003_LoginDDT extends BaseClass {

	@Test(dataProvider = "LoginData", dataProviderClass = DataProviders.class, groups = { "Datadriven", "Master" })
	public void verivy_loginDDT(String email, String pwd, String exp) {
		logger.info("******* Starting TC003_LoginDDT *********");

		if (email == null || pwd == null || exp == null || email.isBlank() || pwd.isBlank() || exp.isBlank()) {
			logger.warn("Skipping test case: one or more input values are blank. [email=" + email + ", pwd=" + pwd
					+ ", exp=" + exp + "]");
			Assert.fail("Test data contains blank values.");
			return;
		}

		try {
			// Reset to home so each Excel row starts from a clean entry point
			driver.get(p.getProperty("appURL1").trim());

			HomePage hp = new HomePage(driver);
			hp.clickMyAccount();
			hp.clickLogin();

			LoginPage lp = new LoginPage(driver);
			lp.setEmail(email.trim());
			lp.setPassword(pwd.trim());
			lp.clickLogin();

			MyAccountPage macc = new MyAccountPage(driver);
			boolean targetPage = macc.isMyAccountPageExists();

			if (exp.trim().equalsIgnoreCase("Valid")) {
				if (targetPage) {
					macc.clickLogout();
					Assert.assertTrue(true, "Login succeeded as expected");
				} else {
					Assert.fail("Login failed, but expected success for: " + email);
				}
			} else if (exp.trim().equalsIgnoreCase("Invalid")) {
				if (targetPage) {
					macc.clickLogout();
					Assert.fail("Login succeeded, but expected failure for: " + email);
				} else {
					Assert.assertTrue(true, "Login failed as expected");
				}
			} else {
				Assert.fail("Unexpected expected-result value in Excel: " + exp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}

		logger.info("******* Finished TC003_LoginDDT *********");
	}
}
