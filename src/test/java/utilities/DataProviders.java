package utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.DataProvider;

public class DataProviders {

	@DataProvider(name = "LoginData")
	public String[][] getData() throws IOException {
		Path path = resolveLoginDataPath();
		ExcelUtility xlutil = new ExcelUtility(path.toString());

		int totalrows = xlutil.getRowCount("Sheet1");
		int totalcols = xlutil.getCellCount("Sheet1", 1);
		String[][] logindata = new String[totalrows][totalcols];

		for (int i = 1; i <= totalrows; i++) {
			for (int j = 0; j < totalcols; j++) {
				logindata[i - 1][j] = xlutil.getCellData("Sheet1", i, j);
			}
		}
		return logindata;
	}

	private static Path resolveLoginDataPath() throws IOException {
		Path[] candidates = new Path[] {
				Paths.get("testData", "Opencart_LoginData.xlsx"),
				Paths.get("testData", "opencart_LoginData.xlsx"),
				Paths.get(".", "testData", "Opencart_LoginData.xlsx"),
		};
		for (Path candidate : candidates) {
			if (Files.exists(candidate)) {
				return candidate.toAbsolutePath().normalize();
			}
		}
		throw new IOException("Login Excel not found under testData/ (expected Opencart_LoginData.xlsx)");
	}
}
