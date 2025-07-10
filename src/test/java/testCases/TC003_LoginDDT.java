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
   				login failed - test fail
   
   Data is invalid - login success - test fail - logout
   				login failed - test pass
 
  
  
 */

public class TC003_LoginDDT extends  BaseClass {
	
	@Test(dataProvider="LoginData", dataProviderClass= DataProviders.class, groups="Datadriven")
	public void verivy_loginDDT(String email, String pwd, String exp) {
		logger.info("******* Starting TC003_LoginDDT *********");
		
		if (email == null || pwd == null || exp == null) {
		    logger.warn("Skipping test case: one or more input values are null. [email=" + email + ", pwd=" + pwd + ", exp=" + exp + "]");
		    Assert.fail("Test data contains null values.");
		    return;
		}


		try {
			// Home Page
			HomePage hp = new HomePage(driver);
			hp.clickMyAccount();
			hp.clickLogin();

			// Login Page
			LoginPage lp = new LoginPage(driver);
			lp.setEmail(email);      //  Use direct values
			lp.setPassword(pwd);     //  Use direct values
			lp.clickLogin();

			// MyAccount
			MyAccountPage macc = new MyAccountPage(driver);
			boolean targetPage = macc.isMyAccountPageExists();

			if (exp.equalsIgnoreCase("Valid")) {
				if (targetPage) {
					macc.clickLogout();
					Assert.assertTrue(true, "Login succeeded as expected");
				} else {
					Assert.fail("Login failed, but expected success");
				}
			} else if (exp.equalsIgnoreCase("Invalid")) {
				if (targetPage) {
					macc.clickLogout();
					Assert.fail("Login succeeded, but expected failure");
				} else {
					Assert.assertTrue(true, "Login failed as expected");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}

		logger.info("******* Finished TC003_LoginDDT *********");
	}


}
