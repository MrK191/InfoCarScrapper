import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

class InfoCarAuthentication {

    String RESERVATION_URL = "https://info-car.pl/infocar/konto/word/rezerwacja.html";

    private final ConfigService configService;

    public InfoCarAuthentication(ConfigService configService) {
        this.configService = configService;
    }

    void login(ChromeDriver chromeDriver) {
        chromeDriver.get(RESERVATION_URL);
        chromeDriver.findElement(By.className("redBtn")).click();
        chromeDriver.findElement(By.className("login-input")).sendKeys(configService.retrieveProperty(ConfigProperty.LOGIN));
        chromeDriver.findElement(By.className("password-input")).sendKeys(configService.retrieveProperty(ConfigProperty.PASSWORD));
        chromeDriver.findElement(By.className("next_arrow")).click();
        chromeDriver.get(RESERVATION_URL);
        chromeDriver.findElement(By.className("redBtn")).click();
        chromeDriver.get(RESERVATION_URL);
    }
}
