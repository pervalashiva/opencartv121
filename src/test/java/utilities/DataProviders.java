package utilities;

import java.io.IOException;

import org.testng.annotations.DataProvider;

public class DataProviders {
	
	@DataProvider(name = "LoginData")
	public String[][] getData() throws IOException {
	    // Path to the Excel file containing the test data
	    String path = ".\\testData\\opencart_LoginData.xlsx";

	    // Create an instance of ExcelUtility to read from the Excel file
	    ExcelUtility xlutil = new ExcelUtility(path);

	    // Get the index of the last row with data in the sheet "Sheet1"
	    // Note: getLastRowNum() returns zero-based index of last row, not count
	    int totalrows = xlutil.getRowCount("Sheet1");

	    // Get the total number of columns in row 1 (assuming row 0 is header)
	    int totalcols = xlutil.getCellCount("Sheet1", 1);

	    // Create a 2D array to hold login data for all rows and columns
	    // Size is totalrows x totalcols because data rows start from index 1 (after header)
	    String[][] logindata = new String[totalrows][totalcols];

	    // Loop through each data row starting from 1 (skip header row 0)
	    // Use <= totalrows to include the last row since totalrows is last row index
	    for (int i = 1; i <= totalrows; i++) {
	        // Loop through each column for the current row
	        for (int j = 0; j < totalcols; j++) {
	            // Read cell data from Excel sheet and store it in the array
	            // i-1 because array index starts from 0, but Excel rows start from 1 for data
	            logindata[i - 1][j] = xlutil.getCellData("Sheet1", i, j);
	        }
	    }
	    // Return the populated 2D array to the DataProvider
	    return logindata;
	}


}
	