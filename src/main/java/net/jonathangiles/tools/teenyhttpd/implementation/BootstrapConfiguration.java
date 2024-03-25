package net.jonathangiles.tools.teenyhttpd.implementation;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class is responsible for reading the configuration properties from the application.properties file
 * and environment variables.
 */
public class BootstrapConfiguration {

    private int serverPort = 8080;
    private Properties properties;

    public BootstrapConfiguration() {
    }

    public void readConfigurations() {
        readPropertyFile();
        readEnvironmentVariables();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, boolean required, Class<T> type) {
        String defaultValue = "";

        if (key.contains(":")) {
            String[] strings = key.split(":");

            key = strings[0];
            defaultValue = strings[1];
        }

        String property = null;

        if (properties != null) {
            property = properties.getProperty(key, defaultValue);
        }

        if (property == null || property.isEmpty()) {
            property = System.getenv(key);
        }

        if (property == null || property.isEmpty()) {
            property = System.getProperty(key);
        }

        if (required && (property == null || property.isEmpty())) {
            throw new RuntimeException("Property " + key + " is required");
        }

        return (T) parseProperty(property, type);
    }

    private Object parseProperty(String value, Class<?> target) {
        if (value == null) return null;

        if (target == String.class) {
            return value;
        }

        if (target.isPrimitive()) {
            return parsePrimitive(value, target);
        }

        if (target == Integer.class) {
            return Integer.parseInt(value);
        }

        if (target == BigDecimal.class) {
            return new BigDecimal(value);
        }

        if (target == BigInteger.class) {
            return new BigInteger(value);
        }

        if (target == Long.class) {
            return Long.parseLong(value);
        }

        if (target == Double.class) {
            return Double.parseDouble(value);
        }

        if (target == Float.class) {
            return Float.parseFloat(value);
        }

        if (target == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        throw new IllegalStateException("Unsupported type " + target.getName());
    }

    /**
     * Parses a value into a primitive target type such as int, long, double, float, or boolean.
     * <p>
     * @param obj the value to parse
     * @param target the target type
     * @return the parsed value or the default value for the target type if the value is null
     * @throws NumberFormatException if the value cannot be parsed into a number if the target is a number
     * @throws IllegalStateException if the target type is not supported
     */
    private Object parsePrimitive(Object obj, Class<?> target) {
        String value = obj == null ? null : obj.toString().trim();

        if (target == int.class) {
            if (value == null) return 0;

            return Integer.parseInt(value);
        }

        if (target == long.class) {
            if (value == null) return 0L;

            return Long.parseLong(value);
        }

        if (target == double.class) {
            if (value == null) return 0.0;

            return Double.parseDouble(value);
        }

        if (target == float.class) {
            if (value == null) return 0.0f;

            return Float.parseFloat(value);
        }

        if (target == boolean.class) {

            if (value == null) return false;

            return Boolean.parseBoolean(value);
        }

        throw new IllegalStateException("Unsupported primitive type: " + target.getName());
    }


    /**
     * Read the configuration properties from the application.properties file
     * this method will be called before reading the environment variables
     */
    private void readPropertyFile() {
        String profile = System.getProperty("profile", "application");

        File configFile = new File(profile + ".properties");
        if (configFile.exists()) {
            // read the file and set the properties
            properties = new Properties();
            try {
                properties.load(new FileReader(configFile));
                serverPort = Integer.parseInt(properties.getProperty("server.port"));
            } catch (Exception e) {
                Logger.getLogger(BootstrapConfiguration.class.getName())
                        .warning("Error reading application.properties file");
            }
        } else {

            InputStream resourceAsStream = BootstrapConfiguration.class.getResourceAsStream("/application.properties");

            if (resourceAsStream != null) {
                properties = new Properties();
                try {
                    properties.load(resourceAsStream);
                    serverPort = Integer.parseInt(properties.getProperty("server.port"));
                } catch (Exception e) {
                    Logger.getLogger(BootstrapConfiguration.class.getName())
                            .warning("Error reading application.properties file");
                }
            }

        }

        if (properties == null) {
            Logger.getLogger(BootstrapConfiguration.class.getName())
                    .warning(configFile.getName() + " file not found");
        }
    }


    /**
     * Read the configuration properties from the environment variables,
     * reading the environment variables will override the properties read from the application.properties file
     */
    private void readEnvironmentVariables() {
        String port = System.getProperty("server.port");
        if (port != null) {
            serverPort = Integer.parseInt(port);
        }
    }

    public int getServerPort() {
        return serverPort;
    }
}
