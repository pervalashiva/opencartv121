package testCases;

import org.testng.Assert;
import org.testng.annotations.Test;

import TestBase.BaseClass;
import pageObjects.HomePage;
import pageObjects.accountRegistrationPage;

public class TC001_AccountRegistrationTest extends BaseClass {
	
	@Test(groups = { "Regression", "Master"})
	void verify_account_registraion()
	{
		logger.info("*******Starting TC001_AccountRegistraionTest *******");
		try
		{
		HomePage hp = new HomePage(driver);
		hp.clickMyAccount();
		logger.info("*******clicked my account *******");

		hp.clickRegister();
		logger.info("*******clicked on register *******");

		accountRegistrationPage regpage = new accountRegistrationPage(driver);
		regpage.setFirstName("Shiva");
		regpage.setLastName("Pervala");
		regpage.setEmail(randomeString() + "@gmail.com");
		regpage.setTelephone(randomeNumber());
		
		//String password = randomAlphanumeric();
		
		String password=randomeAlphaNumberic();
		regpage.setPassword(password);
		regpage.setConfirmPassword(password);
		
		regpage.setPrivacyPolicy();
		regpage.clickContinue();
	 
		logger.info("*******enter all details *******");

		String confmsg=regpage.getConfirmationMsg();
		
		Assert.assertEquals(confmsg, "Your Account Has Been Created!");
		}
		catch(Exception e)
		{
			logger.error("Test Failed..");
			logger.debug("Test to debug");
			Assert.fail();
			
			logger.info("*******Finished *******");

		}
	}
	
}
