import io.github.bonigarcia.wdm.WebDriverManager;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import properties.PropertiesReader;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import BaseTestClass.BaseTestClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class YandexMarketTest {

    final PropertiesReader prop = ConfigFactory.create(PropertiesReader.class);
    private WebDriver driver;
    final Logger logger = LogManager.getLogger(YandexMarketTest.class);
    BaseTestClass base = new BaseTestClass();

    @Before
    public void setUp() {

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1500,800));
        driver.manage().timeouts().implicitlyWait(prop.waitValue(), TimeUnit.SECONDS);
        logger.info("Setup WebDriver");
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void mainYandexMarketTest() {
        driver.get(prop.baseUrl());
        logger.info("Open Page Yandex Market");

        // Поиск по всем производителям
        base.clickXpath(driver, "//fieldset/legend[contains(text(), 'Производитель')]/following-sibling::footer/button[contains(text(), 'Показать всё')]");
        logger.info("Search by manufacturer");

        // Добавление фильтров по производителям
        logger.info("Searching Samsung");
        base.sendKeysById(driver, "7893318-suggester", "Samsung");
        base.findManufacturersInList(driver, "Samsung");

        logger.info("Searching Xiaomi");
        base.sendKeysById(driver, "7893318-suggester", "Xiaomi");
        base.findManufacturersInList(driver, "Xiaomi");

        // Сортировка по цене по возрастанию
        base.clickXpath(driver, "//button[@data-autotest-id=\"dprice\"]");
        logger.info("Sorted by price");

        // Поиск первого смартфона от производителя
        ArrayList<String> productTitles = new ArrayList<String>();
        productTitles.add(base.findFirstPhoneByManufacturerName(driver, "Samsung"));
        productTitles.add(base.findFirstPhoneByManufacturerName(driver, "Xiaomi"));

        // Переход в сравнение товаров
        int comparedProducts = base.checkComparedProducts(driver, productTitles);
        assertTrue(comparedProducts > 1);

        // Открыть все характеристики
        base.clickXpath(driver, "//div[@data-apiary-widget-id='/content/compareContent/compareToolbar']//button[contains(text(), 'Все характеристики')]");

        // Проверить, что в списке характеристик появилась позиция "Операционная система"
        int osProductFeature = base.checkOSProductFeature(driver);
        assertEquals(1, osProductFeature);

        // Открыть различающиеся характеристики
        base.clickXpath(driver, "//div[@data-apiary-widget-id='/content/compareContent/compareToolbar']//button[contains(text(), 'Различающиеся характеристики')]");
        osProductFeature = base.checkOSProductFeature(driver);
        assertEquals(0, osProductFeature);
    }
}

