import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final ConfigService configService = ConfigService.getInstance();

    public static void main(String[] args) {

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("EXECUTED AT: "+ LocalDateTime.now());
            new InfoCarScrapper().checkAvailableDates();
            System.out.println();
        },0, Long.parseLong(configService.retrieveProperty(ConfigProperty.CHECK_INTERVAL_IN_MINUTES)), TimeUnit.MINUTES);
    }
}
