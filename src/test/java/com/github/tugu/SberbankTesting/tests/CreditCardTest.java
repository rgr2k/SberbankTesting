package com.github.tugu.SberbankTesting.tests;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jxl.*;
import jxl.read.biff.BiffException;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.*;
import org.testng.Assert;

public class CreditCardTest {

	protected FirefoxDriver driver;
	protected Workbook dataWorkbook;

	@BeforeClass
	public void init() throws BiffException, IOException {
		System.setOut(new PrintStream(System.out, true, "utf-8"));
		
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		
		Assert.assertNotNull(getClass().getResource("/test-data.xls"), "Excel data file is missing");
		dataWorkbook = Workbook.getWorkbook(getClass().getResourceAsStream("/test-data.xls"));
	}
	
	
	@Test
	public void testCreditCard() throws BiffException, IOException, InterruptedException {		
		Sheet firstSheet = dataWorkbook.getSheet("StartPage");
		String startPage = firstSheet.getCell(0,0).getContents();
		printLogDelimiter();
		
		// Открываем стартовую страницу
		driver.get(startPage);		
		System.out.println("Стартовая страница открыта");
		printLogDelimiter();
		
		// Проверяем наличие закладок
		ArrayList<String> startPageTabsOnWebsite = getTabsOnStartPage();		
		ArrayList<String> expectedStartPageTabs = getExpectedStartPageTabs(firstSheet);
		Assert.assertEquals(startPageTabsOnWebsite, expectedStartPageTabs,
				"Закладки на главной странице не совпадают");
		System.out.println("Список закладок проверен");
		printLogDelimiter();
		
		// Получаем список ссылок Все продукты и услуги > Кредиты
		System.out.println("Список ссылок 'Все продукты и услуги > Кредиты':");
		for (String link : getCreditLinksFromStartPage()) {
			System.out.println(String.format("  - %s", link));
		}
		printLogDelimiter();
		
		// Переходим на закладку Выбрать карту  > кредитные карты  >
		// универсальные кредитные карты  > премиальные карты
		Actions actions = new Actions(driver);
		actions.moveToElement(driver.findElementByXPath("//a[text()[contains(.,'Выбрать')] "
				+ "and text()[contains(.,'карту')]]"));		
		actions.perform();
		driver.findElementByXPath("(//a[text()[contains(.,'Премиальные карты')]])[2]").click();
		System.out.println("Информация о кредитной карте открыта");
		printLogDelimiter();
		
		// Проверяем правильность информации о кредитной карте
		Sheet creditCardSheet = dataWorkbook.getSheet("CreditCard");
		for (int i = 0; i < creditCardSheet.getRows(); i++) {
			WebElement infoElement =
					driver.findElementByXPath(String.format("//table[@class='product-specific']//tr[%d]//td[1]", i+1));
			WebElement valueElement =
					driver.findElementByXPath(String.format("//table[@class='product-specific']//tr[%d]//td[2]", i+1));
			
			String infoExpected = creditCardSheet.getCell(0, i).getContents();
			String valueExpected = creditCardSheet.getCell(1, i).getContents();
			
			Assert.assertEquals(infoElement.getText(), infoExpected,
					"Название параметра карты не совпадает");
			Assert.assertEquals(valueElement.getText(), valueExpected,
					String.format("Значение параметра '%s' карты не совпадает", infoExpected));			
		}		
		System.out.println("Информация о кредитной карте проверена");
		printLogDelimiter();
	}
	
	private ArrayList<String> getCreditLinksFromStartPage() {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<WebElement> links = 
				new ArrayList<WebElement>(driver.findElementsByXPath("//b[text()='Кредиты']/../../../../div[1]//ul//a"));
		for (WebElement link : links) {
			result.add(link.getText());			
		}
		return result;		
	}
	
	private ArrayList<String> getTabsOnStartPage() {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<WebElement> tabs = new ArrayList<WebElement>(driver.findElementsByClassName("top-link"));
		for (WebElement tab : tabs) {
			result.add(tab.getText());			
		}
		return result;
	}
	
	private ArrayList<String> getExpectedStartPageTabs(Sheet sheet) {
		ArrayList<String> result = new ArrayList<String>();
		Cell[] tabs = sheet.getColumn(1);
		for (Cell cell : tabs) {
			result.add(cell.getContents());			
		}
		return result;
	}
	
	private void printLogDelimiter() {
		System.out.println("#############################################################");
	}
	
	@AfterClass
	public void tearDown() {
		if (driver != null) {
			driver.close();
		}
		
		if (dataWorkbook != null) {
			dataWorkbook.close();
		}
	}

}
