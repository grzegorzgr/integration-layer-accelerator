package tests.utils;

import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TestSettings {
    private static TestSettings testSettings;
    private final Properties properties;

    private TestSettings(Properties properties) {
        this.properties = properties;
    }

    public static synchronized TestSettings getInstance() {
        if (testSettings == null) {
            Properties properties = new Properties();
            try {
                properties.load(TestSettings.class.getClassLoader().getResourceAsStream("application-test.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            testSettings = new TestSettings(properties);

            log.info("Using properties:");
            testSettings.properties.entrySet().forEach(objectObjectEntry -> {
                log.info(objectObjectEntry.getKey() + " = " + testSettings.getProperty((String) objectObjectEntry.getKey()));
            });
        }

        return testSettings;
    }

    public String getProperty(String key) {
        String value = System.getenv(toEnvKeyName(key));
        if (value == null) {
            value = properties.getProperty(key);
        }
        return value;
    }

    private String toEnvKeyName(String key) {
        return key.toUpperCase().replace("-", "").replace(".", "_");
    }
}
