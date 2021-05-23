package SouqSearchTest;

import static org.testng.Assert.assertEquals;
import java.io.*;
import org.apache.poi.xssf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;

public class SouqSearchListing {

	ChromeDriver Drv;
	WebElement TotalSearchResult;
	int numOfPages;

	@BeforeTest
	public void OpenURL() {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\Resources\\chromedriver.exe");
		Drv = new ChromeDriver();
		Drv.manage().window().maximize();
		Drv.navigate().to("https://egypt.souq.com/eg-en/");
	}

	@Test(priority = 0)
	public void TestSearchBoxIsExist() {
		try {
			WebElement SrchBox = Drv.findElement(By.id("search_value"));
			SrchBox.sendKeys("iphone");
			SrchBox.submit();
		} catch (NoSuchElementException e) {
			System.out.println("The TestSearchBoxIsExist is not found plz use another locator ");
		}
	}

	@Test(priority = 1) // Or can use (dependsOnMethods = "TestSearchBoxIsExist")
	public void CheckTheTotalSearchResult() throws InterruptedException {
		try {
			TotalSearchResult = Drv.findElement(By.className("total"));
			assertEquals(TotalSearchResult.getText(), "(20 Items found)");

			numOfPages = (Integer.parseInt(TotalSearchResult.getText().replaceAll("\\D+", "")) / 59) + 1;
			System.out.println("Fel total reuslt " + numOfPages);

		} catch (NoSuchElementException e) {
			System.out.println("The total search result is not found plz use another locator ");
		}
	}

	@Test(dependsOnMethods = "CheckTheTotalSearchResult", enabled = false)
	public void Findnext() throws InterruptedException {
		WebElement NextLink;
		JavascriptExecutor JS;
		JS = (JavascriptExecutor) Drv;

		java.util.List<WebElement> Items = Drv
				.findElements(By.cssSelector("div.column.column-block.block-list-large.single-item"));

		WebElement ItemScroll = Items.get(Items.size() - 1);
		JS.executeScript("arguments[0].scrollIntoView();", ItemScroll);
		Thread.sleep(10000);
		java.util.List<WebElement> Updated = Drv
				.findElements(By.cssSelector("div.column.column-block.block-list-large.single-item"));
		WebElement ItemScroll2 = Updated.get(Updated.size() - 1);
		JS.executeScript("arguments[0].scrollIntoView();", ItemScroll2);

		Thread.sleep(5000);

		NextLink = Drv.findElement(By.partialLinkText("Next"));
		NextLink.click();
		Thread.sleep(5000);

	}

	@Test(dependsOnMethods = "TestSearchBoxIsExist", enabled = true) // Or can use (dependsOnMethods = "TestSearchBoxIsExist")
	public void ListingSearchResult() throws InterruptedException, FileNotFoundException, IOException {
		try {

			//For the Excel sheet 
			String path = (System.getProperty("user.dir")+"\\src\\main\\java\\data\\SearchResultFile.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(path));
			XSSFSheet sheet = wb.getSheet("Sheet1");

			// For scrolling 
			JavascriptExecutor JS;
			JS = (JavascriptExecutor) Drv;
			WebElement ItemScroll2;

			// Elements to be fetched 
			WebElement Title, Price;
			int itemNum = 1;	

			//For paging 
			WebElement NextLink,ItemScroll;
			java.util.List<WebElement> Items;
			boolean NextBtnDisplay = false;

			do {
				// initial array of items before scrolling 
				Items = Drv.findElements(By.cssSelector("div.column.column-block.block-list-large.single-item"));

				ItemScroll =  Items.get(Items.size()-1); 
				JS.executeScript("arguments[0].scrollIntoView();", ItemScroll);
				Thread.sleep(2000);

				java.util.List<WebElement> Updated = Drv.findElements(By.cssSelector("div.column.column-block.block-list-large.single-item"));
				for (WebElement Item : Updated) 
				{
					Title = Item.findElement(By.className("itemTitle"));
					Price = Item.findElement(By.cssSelector("h3.itemPrice"));
					System.out.println("Page" +" item num " +itemNum+ "  "+ Title.getText() + "    For Price " + Price.getText());
					sheet.createRow(itemNum);
					sheet.getRow(itemNum).createCell(0).setCellValue( itemNum+ "  "+ Title.getText());
					sheet.getRow(itemNum).createCell(1).setCellValue(Price.getText());
					itemNum++;
				}

				ItemScroll2 =  Updated.get(Updated.size()-1); 
				JS.executeScript("arguments[0].scrollIntoView();", ItemScroll2);
				Thread.sleep(2000);

				try 
				{
					if(Drv.findElement(By.partialLinkText("Next")).isDisplayed())
					{
						NextLink = Drv.findElement(By.partialLinkText("Next"));
						NextLink.click();
						Thread.sleep(2000);
						NextBtnDisplay =true;
					}
				}
				catch (Exception e) 
				{
					break;
				}
			}

			while (NextBtnDisplay);

			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir")+"\\src\\main\\java\\data\\SearchResultFile.xlsx");
			wb.write(fos);
			fos.close();	

		}

		catch(

				NoSuchElementException e)
		{
			System.out.println("The element is not found plz use another locator ");
		}
	}

	@AfterTest
	public void CloseDriver() {
		Drv.quit();
	}

}
