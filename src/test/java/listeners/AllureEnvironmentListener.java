package listeners;

import common.env.ConfigFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Properties;

/**
 * TestNG listener that generates environment.properties file for Allure reports.
 * This file contains all test configuration properties and will be displayed on the Allure dashboard.
 */
public class AllureEnvironmentListener implements IExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AllureEnvironmentListener.class);
    private static final String ENVIRONMENT_FILE_NAME = "environment.properties";
    
    @Override
    public void onExecutionFinish() {
        try {
            generateEnvironmentFile();
            logger.info("Successfully generated environment.properties file for Allure report");
        } catch (Exception e) {
            logger.error("Failed to generate environment.properties file", e);
        }
    }
    
    private void generateEnvironmentFile() throws IOException {
        // Get Allure results directory from system property or use default
        String allureResultsDir = System.getProperty("allure.results.directory", "target/allure-results");
        Path allureResultsPath = Paths.get(allureResultsDir);
        
        // Create directory if it doesn't exist
        if (!Files.exists(allureResultsPath)) {
            Files.createDirectories(allureResultsPath);
        }
        
        // Create environment.properties file
        Path environmentFile = allureResultsPath.resolve(ENVIRONMENT_FILE_NAME);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(environmentFile.toFile()))) {
            writer.println("# Allure Environment Properties");
            writer.println("# Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            
            // Test Thread Count (from Owner config)
            writer.println("Test Thread Count=" + ConfigFactoryProvider.appConfig().threadCount());
            writer.println();
            
            // API Base URL and Endpoints
            writer.println("Base URL=" + ConfigFactoryProvider.apiConfig().baseUrl());
            writer.println("Default Supervisor Login=" + ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
            writer.println("Default Admin Login=" + ConfigFactoryProvider.apiConfig().defaultAdminLogin());
            writer.println("Player Create Endpoint=" + ConfigFactoryProvider.apiConfig().endpointPlayerCreate());
            writer.println("Player Get Endpoint=" + ConfigFactoryProvider.apiConfig().endpointPlayerGet());
            writer.println("Player Get All Endpoint=" + ConfigFactoryProvider.apiConfig().endpointPlayerGetAll());
            writer.println("Player Update Endpoint=" + ConfigFactoryProvider.apiConfig().endpointPlayerUpdate());
            writer.println("Player Delete Endpoint=" + ConfigFactoryProvider.apiConfig().endpointPlayerDelete());
            writer.println();
            
            // All properties from environment config file
            writer.println("# Environment Configuration");
            
            // Dynamically read all properties from the config file
            try {
                String env = System.getProperty("env", "prod");
                String configPath = "src/main/resources/" + env + "/config.properties";
                File configFile = new File(configPath);
                
                if (configFile.exists()) {
                    Properties configProperties = new Properties();
                    configProperties.load(new java.io.FileInputStream(configFile));
                    
                    Iterator<String> propertyNames = configProperties.stringPropertyNames().iterator();
                    while (propertyNames.hasNext()) {
                        String propertyName = propertyNames.next();
                        String propertyValue = configProperties.getProperty(propertyName);
                        // Convert property name to display format (e.g., "base.url" -> "Base URL")
                        String displayName = convertToDisplayName(propertyName);
                        writer.println(displayName + "=" + propertyValue);
                    }
                } else {
                    logger.warn("Config file not found: {}", configPath);
                }
            } catch (Exception e) {
                logger.error("Error reading config file properties", e);
            }
        }
        
        logger.info("Environment properties file created at: {}", environmentFile.toAbsolutePath());
    }
    
    /**
     * Converts a property name to a display-friendly format.
     * Example: "base.url" -> "Base URL", "test.thread.count" -> "Test Thread Count"
     */
    private String convertToDisplayName(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            return propertyName;
        }
        
        // Split by dots and capitalize each word
        String[] words = propertyName.split("\\.");
        StringBuilder displayName = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                displayName.append(" ");
            }
            
            String word = words[i];
            if (!word.isEmpty()) {
                // Capitalize first letter
                displayName.append(Character.toUpperCase(word.charAt(0)));
                // Add rest of the word
                if (word.length() > 1) {
                    displayName.append(word.substring(1));
                }
            }
        }
        
        return displayName.toString();
    }
}
