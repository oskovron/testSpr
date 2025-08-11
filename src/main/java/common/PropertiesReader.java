package common;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Utility class for reading configuration properties from various sources.
 */
public class PropertiesReader {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);
    private static Configuration configuration;
    
    static {
        try {
            loadConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Failed to load configuration", e);
        }
    }
    
    private static void loadConfiguration() throws ConfigurationException {
        Configurations configs = new Configurations();
        
        // Try to load from prod config first
        String configPath = "src/main/resources/prod/config.properties";
        File configFile = new File(configPath);
        
        if (configFile.exists()) {
            configuration = configs.properties(configFile);
            logger.info("Loaded configuration from: {}", configPath);
        } else {
            // Fallback to default config
            configPath = "src/main/resources/config.properties";
            configFile = new File(configPath);
            if (configFile.exists()) {
                configuration = configs.properties(configFile);
                logger.info("Loaded configuration from: {}", configPath);
            } else {
                logger.warn("No configuration file found, using system properties only");
                configuration = null;
            }
        }
    }
    
    /**
     * Get a property value with fallback to system properties and environment variables.
     * 
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value
     */
    public static String getProperty(String key, String defaultValue) {
        // Try configuration file first
        if (configuration != null) {
            String value = configuration.getString(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        // Try system properties (from Maven)
        String value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        // Try environment variables
        value = System.getenv(key.replace(".", "_").toUpperCase());
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        // Return default value
        return defaultValue;
    }
    
    /**
     * Get a property value as integer.
     * 
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value as integer
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a property value as boolean.
     * 
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value as boolean
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}
