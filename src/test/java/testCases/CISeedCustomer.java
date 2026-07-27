package testCases;

import org.testng.Assert;
import org.testng.annotations.Test;

import TestBase.BaseClass;
import pageObjects.HomePage;
import pageObjects.MyAccountPage;
import pageObjects.accountRegistrationPage;

/**
 * Registers a fixed customer used by TC002 / TC003 Valid rows in CI Docker OpenCart.
 */
public class CISeedCustomer extends BaseClass {

	@Test
	public void seedCustomerAccount() {
		String email = firstNonBlank(System.getenv("TEST_EMAIL"), p.getProperty("email"));
		String password = firstNonBlank(System.getenv("TEST_PASSWORD"), p.getProperty("password"));

		Assert.assertNotNull(email, "TEST_EMAIL / config email is required for seeding");
		Assert.assertNotNull(password, "TEST_PASSWORD / config password is required for seeding");

		HomePage hp = new HomePage(driver);
		hp.clickMyAccount();
		hp.clickRegister();

		accountRegistrationPage reg = new accountRegistrationPage(driver);
		reg.setFirstName("CI");
		reg.setLastName("User");
		reg.setEmail(email.trim());
		reg.setTelephone("9876543210");
		reg.setPassword(password.trim());
		reg.setConfirmPassword(password.trim());
		reg.setPrivacyPolicy();
		reg.clickContinue();

		String conf = reg.getConfirmationMsg();
		boolean created = conf != null && conf.contains("Your Account Has Been Created");
		boolean alreadyExists = driver.getPageSource().toLowerCase().contains("already registered")
				|| driver.getPageSource().toLowerCase().contains("e-mail address is already registered");

		if (!created && !alreadyExists) {
			// Some OpenCart builds show warning alerts instead of the success H1
			MyAccountPage account = new MyAccountPage(driver);
			created = account.isMyAccountPageExists();
		}

		Assert.assertTrue(created || alreadyExists,
				"Unable to seed customer. confirmation='" + conf + "' title='" + driver.getTitle() + "'");
		logger.info("Seeded / confirmed customer account for {}", email);
	}

	private static String firstNonBlank(String a, String b) {
		if (a != null && !a.isBlank()) return a.trim();
		if (b != null && !b.isBlank()) return b.trim();
		return null;
	}
}
