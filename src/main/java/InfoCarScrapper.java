import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class InfoCarScrapper {
    private final ConfigService configService;
    private final InfoCarAuthentication infoCarAuthentication;

    public InfoCarScrapper() {
        this.configService = ConfigService.getInstance();
        this.infoCarAuthentication = new InfoCarAuthentication(configService);
    }

    void checkAvailableDates() {
        ChromeDriver chromeDriver = prepareChromeDriver();

        try {
            WebDriverWait waiter = new WebDriverWait(chromeDriver, 9);

            infoCarAuthentication.login(chromeDriver);
            chooseWORD(chromeDriver);
            chromeDriver
                    .findElement(By.cssSelector("#examTypeContainer div > div:nth-child(1) a:nth-child(2)"))
                    .click();

            String month = extractMonthText(chromeDriver, waiter);
            SingleMonthElement currentMonth = new SingleMonthElement(month);
            List<WebElement> daysOfCurrentMonthAvailable = waiter.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#schedule td.available"), 0));

            for (WebElement singleDay : daysOfCurrentMonthAvailable) {
                scrapSingleDay(chromeDriver, waiter, singleDay, currentMonth);

                JavascriptExecutor jse = chromeDriver;
                jse.executeScript("arguments[0].click();", chromeDriver.findElementByCssSelector("#simplemodal-overlay"));
            }

            clickNextButton(chromeDriver);

            String nextMonth = extractMonthText(chromeDriver, waiter);
            SingleMonthElement nextMonthElement = new SingleMonthElement(nextMonth);
            List<WebElement> daysOfNextMonthAvailable = waiter.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#schedule td.available"), 0));

            for (WebElement singleDay : daysOfNextMonthAvailable) {
                scrapSingleDay(chromeDriver, waiter, singleDay, nextMonthElement);

                JavascriptExecutor jse = chromeDriver;
                jse.executeScript("arguments[0].click();", chromeDriver.findElementByCssSelector("#simplemodal-overlay"));
            }

            printAvailableDays(currentMonth, nextMonthElement);

        } catch (Exception ex) {
            System.out.println("THERE WAS SOME SERIOUS ERROR, CONTACT THE AUTHOR");
            System.out.println(ex);
            chromeDriver.close();
        }

        chromeDriver.close();
    }

    private void printAvailableDays(SingleMonthElement currentMonth, SingleMonthElement nextMonth) {
        String monthSplitter = "--------";

        System.out.println(currentMonth.getMonth());
        System.out.println(monthSplitter);
        printGivenMonth(currentMonth.getAvailableDays());

        System.out.println(nextMonth.getMonth());
        System.out.println(monthSplitter);
        printGivenMonth(nextMonth.getAvailableDays());
    }

    private void printGivenMonth(HashMap<Integer, List<String>> currentMonthDays) {

        currentMonthDays.keySet()
                        .stream()
                        .sorted()
                        .forEach(key -> {
            System.out.print(key + "- ");
            currentMonthDays.get(key).forEach(value -> System.out.print(value + ","));
            System.out.println();
        });

        if (currentMonthDays.isEmpty()) {
            System.out.println("There were no days available to register");
        }
    }

    private String extractMonthText(ChromeDriver chromeDriver, WebDriverWait waiter) {
        WebElement element = chromeDriver.findElement(By.cssSelector("#scheduleCurrentMonth :nth-child(2)"));
        waiter.until(webDriver -> ExpectedConditions.stalenessOf(element).apply(webDriver) ||
                !webDriver.findElement(By.cssSelector("#scheduleCurrentMonth :nth-child(2)")).getText().isEmpty());

        String text = element.getText();
        return text.replaceAll(" \\d*", "");
    }

    private void clickNextButton(ChromeDriver chromeDriver) {
        chromeDriver
                .findElement(By.cssSelector("#scheduleCurrentMonth .scheduleNextMonthLink"))
                .click();
    }

    private void scrapSingleDay(ChromeDriver chromeDriver,
                                WebDriverWait waiter,
                                WebElement singleDay,
                                SingleMonthElement singleMonthElement) {
        waiter.until(ExpectedConditions.elementToBeClickable(singleDay)).click();
        List<WebElement> practiceTestHours = chromeDriver.findElementsByCssSelector("#selectedDayTerms tr > td:nth-child(2)");
        HashMap<Integer, List<String>> availableDays = singleMonthElement.getAvailableDays();

        practiceTestHours.forEach(practiceTestElement -> {
            String practiceElementClass = practiceTestElement.getAttribute("class");

            Optional.ofNullable(practiceElementClass)
                    .filter(classValue -> !classValue.equals("notAvailable"))
                    .ifPresent(s -> {
                        String examHour = practiceTestElement.findElement(By.className("examTypeHour")).getText();
                        Integer day = Integer.parseInt(singleDay.getText());
                        List<String> existingDay = availableDays.getOrDefault(day, new ArrayList<>());
                        existingDay.add(examHour);
                        availableDays.put(day, existingDay);
                    });
        });
    }

    private ChromeDriver prepareChromeDriver() {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);

        String showBrowser = configService.retrieveProperty(ConfigProperty.SHOW_BROWSER);

        return showBrowser.equals("YES") ? new ChromeDriver() : new ChromeDriver(new ChromeOptions().addArguments("--window-position=-2220,-2221"));
    }

    private void chooseWORD(ChromeDriver chromeDriver) {
        chromeDriver.findElement(By.className("redBtn")).click();

        String wordLocationProperty = configService.retrieveProperty(ConfigProperty.WORD_LOCATION);
        InfoCarWORD wordLocation = InfoCarWORD.valueOf(wordLocationProperty);

        new Select(chromeDriver.findElement(By.cssSelector("#wordSelector select:nth-child(2)"))).selectByValue(String.valueOf(wordLocation.wordId));
        chromeDriver.findElement(By.linkText("wybierz ten")).click();
    }
}
