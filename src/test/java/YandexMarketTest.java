import io.github.bonigarcia.wdm.WebDriverManager;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import properties.PropertiesReader;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class YandexMarketTest {

    private PropertiesReader prop = ConfigFactory.create(PropertiesReader.class);
    protected static WebDriver driver;
    private Logger logger = LogManager.getLogger(YandexMarketTest.class);

    public void clickXpath (String locator){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
        driver.findElement(By.xpath(locator)).click();
    }

    public void waitXpath (String locator){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
//        driver.findElement(By.xpath(locator)).click();
    }

    public void clickCss (String locator){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(locator)));
        driver.findElement(By.cssSelector(locator)).click();
    }


    public void sendKeysById (String locator, String keys){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
        driver.findElement(By.id(locator)).clear();
        driver.findElement(By.id(locator)).sendKeys(keys);
    }

    public void findManufacturersInList(String manufacturer) throws InterruptedException {
        logger.info(String.format("Waiting for %s", manufacturer));
        WebElement filteredManufacturer = null;
        Boolean hasText = false;
        int tries = 1;
        while (hasText == false && tries < 20) {
            try {
                logger.info(String.format("Attempt no. %s", tries));
                filteredManufacturer = driver.
                        findElement(By.xpath
                                ("//*[@id=\"search-prepack\"]/div/div[3]/div/div/div[2]/div[2]/div/div/fieldset/ul/li"));
                WebDriverWait wait = new WebDriverWait(driver, 1);
                wait.until(ExpectedConditions
                        .textToBePresentInElement(filteredManufacturer, manufacturer));
                hasText = true;
            } catch (TimeoutException e) {
                logger.info(String.format("Not found"));
                tries++;
            }

        }
        logger.info(String.format("Found %s", filteredManufacturer.getText()));
        if (filteredManufacturer.getText().equals(manufacturer)) {
            filteredManufacturer.findElement(By.xpath(".//div/a/label")).click();
            logger.info(String.format("Added filter by manufacturer %s", filteredManufacturer.getText()));
        }
    }

    public void findFirstPhoneByManufacturerName(String manufacturer){
        driver.navigate().refresh();
        logger.info("Searching product snippets");
        List <WebElement> phoneList = driver.findElements(By.xpath("//*[@data-autotest-id=\"product-snippet\"]"));
        logger.info(String.format("Found %s product snippets", phoneList.size()));
        int tries = 1;
        for (WebElement elem : phoneList) {
            logger.info(String.format("Attempt no. %s", tries));
            if (elem.getText().contains(manufacturer)) {
                logger.info(String.format("Found first phone %s", manufacturer));
                elem.findElement(By.xpath(".//div/div/div")).click();

                String productTitle = elem.findElement(By.xpath(".//h3")).getText();
                logger.info(String.format("%s added to compare list", productTitle));
                WebDriverWait wait = new WebDriverWait(driver, 10);

                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[2]/div[3]/div[6]/div")));
                String modalWindowText = driver.findElement(By.xpath("/html/body/div[2]/div[3]/div[6]/div/div")).getText();
                String expectedModalWindowText = String.format("Товар %s добавлен к сравнению", productTitle);
                assertTrue(modalWindowText
                        .contains(expectedModalWindowText));
                break;
            } else {
                tries++;
            }
        }
    }

    public int checkComparedProducts(){
        driver.get("https://market.yandex.ru/my/compare-lists");
        List<WebElement> compareList = driver
                .findElements(
                        By.xpath("//div[@data-apiary-widget-id=\"/content/compareContent\"]/div/div[2]/div/div/div/div"));

        logger.info(String.format("Found %s compared products", compareList.size()));
        return compareList.size();
    }


    public int checkOSProductFeachure() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions
                .presenceOfElementLocated
                        (By.xpath("//div[@data-apiary-widget-id=\"/content/compareContent\"]/div/div[5]/div")));

        List<WebElement> productFeatures = driver.findElements
                (By.xpath("//div[@data-apiary-widget-id=\"/content/compareContent\"]/div/div[5]/div"));

        int foundOSFeachure = 0;
        for (WebElement elem : productFeatures) {
            String feachureName = elem.findElement(By.xpath(".//div")).getText();
            if (feachureName.toLowerCase().equals("операционная система")) {
                logger.info("Found feachure \"Операционная система\"");
                foundOSFeachure++;
                break;
            }
        }
        logger.info(String.format("Found %s OS feachures", foundOSFeachure));
        return foundOSFeachure;
    }


    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
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
    public void mainYandexMarketTest() throws InterruptedException {
        driver.get("https://market.yandex.ru/catalog--mobilnye-telefony/54726/list?hid=91491&onstock=1&local-offers-first=0");
        logger.info("Open Page Yandex Market");

        clickXpath("//*[@id=\"search-prepack\"]/div/div[3]/div/div/div[2]/div[2]/div/div/fieldset/footer/button");


        //Добавление фильров по производителям
        sendKeysById("7893318-suggester", "Samsung");
        findManufacturersInList("Samsung");

        sendKeysById("7893318-suggester", "Xiaomi");
        findManufacturersInList("Xiaomi");

        //Сортировка по цене по возрастанию
        clickXpath("//button[@data-autotest-id=\"dprice\"]");
        logger.info("Sorted by price");

        findFirstPhoneByManufacturerName("Samsung");
        findFirstPhoneByManufacturerName("Xiaomi");

        //Переход в сравнение товаров
        int comparedProducts = checkComparedProducts();
        assertTrue(comparedProducts > 1);

        //Открыть все характеристики
        clickXpath("//div[@data-apiary-widget-id=\"/content/compareContent/compareToolbar\"]/div/div/button[2]");

        //Проверить, что в списке характеристик появилась позиция "Операционная система"
        int osProductFeachure = checkOSProductFeachure();
        assertTrue(osProductFeachure > 0);

        //Открыть различающиеся характеристики
        clickXpath("//div[@data-apiary-widget-id=\"/content/compareContent/compareToolbar\"]/div/div/button[1]");
        osProductFeachure = checkOSProductFeachure();
        assertEquals(0, osProductFeachure);

    }
}

