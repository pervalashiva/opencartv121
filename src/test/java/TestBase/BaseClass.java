package TestBase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

public class BaseClass {
	public static WebDriver driver;
	public Logger logger; // log4j
	public Properties p;

	@BeforeClass(groups = { "Sanity", "Regression", "Master", "Datadriven" }, alwaysRun = true)
	@Parameters({ "os", "browser" })
	public void setup(String os, String br) throws IOException, URISyntaxException {

		// loading config .properties file
		FileReader file = new FileReader("./src/test/resources/config.properties");
		p = new Properties();
		p.load(file);

		logger = LogManager.getLogger(this.getClass()); // log4j

		boolean headless;
		if (System.getenv("HEADLESS") != null) {
			headless = Boolean.parseBoolean(System.getenv("HEADLESS"));
		} else if (System.getProperty("HEADLESS") != null) {
			headless = Boolean.parseBoolean(System.getProperty("HEADLESS"));
		} else {
			// Local default: headed. GitHub Actions / CI default: headless.
			headless = "true".equalsIgnoreCase(System.getenv("CI"))
					|| "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"));
		}

		if (p.getProperty("execution_env").trim().equalsIgnoreCase("remote")) {
			DesiredCapabilities capabilities = new DesiredCapabilities();

			if (os.equalsIgnoreCase("windows")) {
				capabilities.setPlatform(Platform.WIN11);
			} else if (os.equalsIgnoreCase("mac")) {
				capabilities.setPlatform(Platform.MAC);
			} else if (os.equalsIgnoreCase("linux")) {
				capabilities.setPlatform(Platform.LINUX);
			} else {
				System.out.println("No matching os");
				return;
			}

			switch (br.toLowerCase()) {
			case "chrome":
				capabilities.setBrowserName("chrome");
				break;
			case "edge":
				capabilities.setBrowserName("microsoftEdge");
				break;
			case "firefox":
				capabilities.setBrowserName("firefox");
				break;
			default:
				System.out.println("No matching browser");
				return;
			}
			driver = new RemoteWebDriver(new URI("http://localhost:4444/wd/hub").toURL(), capabilities);
		}

		if (p.getProperty("execution_env").trim().equalsIgnoreCase("local")) {
			switch (br.toLowerCase()) {
			case "chrome":
				ChromeOptions chromeOptions = new ChromeOptions();
				if (headless) {
					chromeOptions.addArguments("--headless=new");
				}
				chromeOptions.addArguments("--window-size=1920,1080", "--disable-gpu", "--no-sandbox",
						"--disable-dev-shm-usage", "--remote-allow-origins=*");
				driver = new ChromeDriver(chromeOptions);
				break;
			case "edge":
				EdgeOptions edgeOptions = new EdgeOptions();
				if (headless) {
					edgeOptions.addArguments("--headless=new");
				}
				edgeOptions.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");
				driver = new EdgeDriver(edgeOptions);
				break;
			case "firefox":
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				if (headless) {
					firefoxOptions.addArguments("-headless");
				}
				driver = new FirefoxDriver(firefoxOptions);
				break;
			default:
				System.out.println("Invalid browser name...");
				return;
			}
		}

		if (driver == null) {
			System.out.println("WebDriver was not created for browser: " + br);
			return;
		}

		driver.manage().deleteAllCookies();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.get(p.getProperty("appURL1").trim());
		driver.manage().window().maximize();
	}

	@AfterClass(groups = { "Sanity", "Regression", "Master", "Datadriven" }, alwaysRun = true)
	public void tearDown() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	public String randomeString() {
		String generatedstring = RandomStringUtils.randomAlphabetic(5);
		return generatedstring;
	}

	public String randomeNumber() {
		String generatedstring = RandomStringUtils.randomNumeric(10);
		return generatedstring;
	}

	public String randomeAlphaNumberic() {
		String generatedstring = RandomStringUtils.randomAlphabetic(5);
		String generatednumber = RandomStringUtils.randomNumeric(5);
		return (generatedstring + "@" + generatednumber);
	}

	public static String captureScreenshot(String tname) throws IOException {
		if (driver == null) {
			throw new IOException("WebDriver is null; cannot capture screenshot");
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddhmmss").format(new Date());
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
		File shotsDir = new File(System.getProperty("user.dir"), "screenshots");
		if (!shotsDir.exists()) {
			shotsDir.mkdirs();
		}
		String targetFilePath = shotsDir.getAbsolutePath() + File.separator + tname + "_" + timeStamp + ".png";
		File targetFile = new File(targetFilePath);
		sourceFile.renameTo(targetFile);
		return targetFilePath;
	}

}
