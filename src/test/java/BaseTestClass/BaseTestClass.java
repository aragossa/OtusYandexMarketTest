package BaseTestClass;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import properties.PropertiesReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BaseTestClass {

    final PropertiesReader prop = ConfigFactory.create(PropertiesReader.class);
    final Logger logger = LogManager.getLogger(BaseTestClass.class);

    public void clickXpath (WebDriver driver, String locator){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
        driver.findElement(By.xpath(locator)).click();
    }

    public void sendKeysById (WebDriver driver, String locator, String keys){
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
        driver.findElement(By.id(locator)).clear();
        driver.findElement(By.id(locator)).sendKeys(keys);
    }

    public void findManufacturersInList(WebDriver driver, String manufacturer) {
        logger.info("Waiting for {}", manufacturer);
        WebElement filteredManufacturer = null;
        boolean hasText = false;
        int tries = 1;
        while (!hasText  && tries < 20) {
            try {
                logger.info(String.format("Attempt no. %s", tries));
                filteredManufacturer = driver.
                        findElement(By.xpath
                                ("//fieldset/legend[contains(text(), 'Производитель')]/following-sibling::ul/li"));
                WebDriverWait wait = new WebDriverWait(driver, 1);
                wait.until(ExpectedConditions
                        .textToBePresentInElement(filteredManufacturer, manufacturer));
                hasText = true;
            } catch (TimeoutException e) {
                logger.info("Not found");
                tries++;
            }

        }
        assertTrue(hasText);
        logger.info("Found {}", filteredManufacturer.getText());
        assertEquals(manufacturer, filteredManufacturer.getText());
        filteredManufacturer.findElement(By.xpath(".//div/a/label")).click();
        logger.info("Added filter by manufacturer {}", filteredManufacturer.getText());

    }

    public String findFirstPhoneByManufacturerName(WebDriver driver, String manufacturer){
        driver.navigate().refresh();
        logger.info("Searching product snippets");
        List<WebElement> phoneList = driver.findElements(By.xpath("//*[@data-autotest-id=\"product-snippet\"]"));
        logger.info("Found {} product snippets", phoneList.size());
        String productTitle = null;
        int tries = 1;
        for (WebElement elem : phoneList) {
            logger.info("Attempt no. {}", tries);
            if (elem.getText().contains(manufacturer)) {
                logger.info("Found first phone {}", manufacturer);
                elem.findElement(By.xpath(".//div/div/div")).click();

                productTitle = elem.findElement(By.xpath(".//h3")).getText();
                logger.info("{} added to compare list", productTitle);
                WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());

                String expectedModalWindowTextXpath = String.format("//div[contains(text(), 'Товар %s добавлен к сравнению')]", productTitle);

                wait.until(ExpectedConditions.
                        presenceOfElementLocated(By.
                                xpath(expectedModalWindowTextXpath)));
                String modalWindowText = driver.findElement(By.xpath(expectedModalWindowTextXpath)).getText();
                String expectedModalWindowText = String.format("Товар %s добавлен к сравнению", productTitle);
                assertTrue(modalWindowText
                        .contains(expectedModalWindowText));
                break;
            } else {
                tries++;
            }
        }
        assertNotNull(productTitle);
        return productTitle;
    }

    public int checkComparedProducts(WebDriver driver, ArrayList<String> productTitles) {
        driver.get("https://market.yandex.ru/my/compare-lists");
        ArrayList<WebElement> compareList = new ArrayList<WebElement>();
        for (String title : productTitles) {
            String xpath = String.format("//div[@data-apiary-widget-id='/content/compareContent']//a[contains(text(), '%s')]", title);
            compareList.add(driver.findElement(By.xpath(xpath)));
        }

        logger.info(String.format("Found %s compared products", compareList.size()));
        return compareList.size();
    }


    public int checkOSProductFeature(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, prop.waitValue());
        wait.until(ExpectedConditions
                .presenceOfElementLocated
                        (By.xpath("//div[@data-apiary-widget-id=\"/content/compareContent\"]/div/div[5]/div")));

        List<WebElement> productFeatures = driver.findElements
                (By.xpath("//div[@data-apiary-widget-id=\"/content/compareContent\"]/div/div[5]/div"));

        int foundOSFeature = 0;
        for (WebElement elem : productFeatures) {
            String featureName = elem.findElement(By.xpath(".//div")).getText();
            if (featureName.equalsIgnoreCase("операционная система")) {
                logger.info("Found feature \"Операционная система\"");
                foundOSFeature++;
                break;
            }
        }
        logger.info(String.format("Found %s OS features", foundOSFeature));
        return foundOSFeature;
    }
}
