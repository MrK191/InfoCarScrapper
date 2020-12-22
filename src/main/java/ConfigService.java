import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

class ConfigService {

    private final Properties config;
    private static ConfigService INSTANCE;

    private ConfigService() {
        this.config = loadResources();
    }

    public static ConfigService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ConfigService();
        }

        return INSTANCE;
    }

    String retrieveProperty(ConfigProperty configProperty) {
        Optional<Object> property = Optional.ofNullable(config.get(configProperty.toString()));
        if (property.isEmpty()) {
            System.out.println("COULD NOT FIND " + configProperty + " PROPERTY");
            throw new NullPointerException("" + configProperty);
        }

        return (String) property.get();
    }

    private Properties loadResources() {
        Properties props = new Properties();
        Path curDir = Paths.get(".");
        System.out.println(curDir.toAbsolutePath());

        try (InputStream resourceStream = Files.newInputStream(Paths.get("./config.properties"))) {
            props.load(resourceStream);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("ERROR - FAILED TO LOAD RESOURCES");
        }

        return props;
    }
}
