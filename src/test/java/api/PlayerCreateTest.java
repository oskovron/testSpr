package api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import api.client.ResponseWrapper;
import api.model.error.ErrorBody;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import util.TestDataGenerator;

public class PlayerCreateTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateTest.class);

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerCreateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();
    }

    @AfterMethod
    public void tearDown() {
        logger.info("Cleaning up {} created players", createdPlayerIds.size());
        for (Integer playerId : createdPlayerIds) {
            try {
                apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), playerId);
                logger.info("Cleaned up player with ID: {}", playerId);
            } catch (Exception e) {
                logger.warn("Failed to clean up player with ID: {}", playerId, e);
            }
        }
    }

    // ==================== VALID CREATE TESTS ====================

    @Test(description = "Create player with valid data using supervisor")
    public void testCreatePlayerWithValidDataUsingSupervisor() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with valid data using supervisor");
        SoftAssert softAssert = new SoftAssert();
        Player expectedPlayer = TestDataGenerator.generateValidPlayer();
        logger.debug("Generated valid player: {}", expectedPlayer);
        
        PlayerResponse actualPlayer = createAndVerifyPlayer(expectedPlayer, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        assertPlayerEquals(actualPlayer, expectedPlayer, softAssert);
        logger.info("Successfully created player with ID: {}", actualPlayer.getPlayerId());
    }

    @Test(description = "Create player with valid data using admin")
    public void testCreatePlayerWithValidDataUsingAdmin() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with valid data using admin");
        SoftAssert softAssert = new SoftAssert();
        Player expectedPlayer = TestDataGenerator.generateValidPlayer();
        logger.debug("Generated valid player: {}", expectedPlayer);
        
        PlayerResponse actualPlayer = createAndVerifyPlayer(expectedPlayer, ConfigFactoryProvider.apiConfig().defaultAdminLogin());
        assertPlayerEquals(actualPlayer, expectedPlayer, softAssert);
        logger.info("Successfully created player with ID: {}", actualPlayer.getPlayerId());
    }

    @Test(description = "Create player with admin role")
    public void testCreatePlayerWithAdminRole() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with admin role");
        Player player = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Generated admin player: {}", player);
        
        ResponseWrapper<PlayerResponse> response = apiClient.createPlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), player);
        assertEquals(response.getResponse().statusCode(), 200, "Couldn't create player.");
        PlayerResponse createdPlayer = response.readEntity();
        createdPlayerIds.add(createdPlayer.getPlayerId());
        logger.info("Successfully created admin player with ID: {}", createdPlayer.getPlayerId());
    }

    @Test(description = "Create player with user role")
    public void testCreatePlayerWithUserRole() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with user role");
        Player player = TestDataGenerator.generateValidPlayer("user");
        logger.debug("Generated user player: {}", player);
        
        ResponseWrapper<PlayerResponse> response = apiClient.createPlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), player);
        assertEquals(response.getResponse().statusCode(), 200, "Couldn't create player.");
        PlayerResponse createdPlayer = response.readEntity();
        createdPlayerIds.add(createdPlayer.getPlayerId());
        logger.info("Successfully created user player with ID: {}", createdPlayer.getPlayerId());
    }

    // ==================== AGE VALIDATION TESTS ====================

    @Test(description = "Create player with invalid age (too young - 15)")
    public void testCreatePlayerWithInvalidAgeTooYoung() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid age (too young)");
        Player player = TestDataGenerator.generatePlayerWithInvalidAgeYoung();
        logger.debug("Generated player with invalid young age: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Player age is too young/old.", "Wrong error title for invalid young age");
    }

    @Test(description = "Create player with invalid age (too old - 61)")
    public void testCreatePlayerWithInvalidAgeTooOld() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid age (too old)");
        Player player = TestDataGenerator.generatePlayerWithInvalidAgeOld();
        logger.debug("Generated player with invalid old age: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Player age is too young/old.", "Wrong error title for invalid old age");
    }

    // ==================== GENDER VALIDATION TESTS ====================

    @Test(description = "Create player with invalid gender")
    public void testCreatePlayerWithInvalidGender() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName()); //todo delete sout thread
        logger.info("Testing player creation with invalid gender");
        Player player = TestDataGenerator.generatePlayerWithInvalidGender();
        logger.debug("Generated player with invalid gender: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Gender can be male/female.", "Wrong error title for invalid gender");
    }

    // ==================== PASSWORD VALIDATION TESTS ====================

    @Test(description = "Create player with invalid password (too short)")
    public void testCreatePlayerWithInvalidPasswordTooShort() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid password (too short)");
        Player player = TestDataGenerator.generatePlayerWithInvalidPasswordShort();
        logger.debug("Generated player with short password: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Password must contain latin letters and numbers (min 7 max 15 characters).", "Wrong error title for short password");
    }

    @Test(description = "Create player with invalid password (too long)")
    public void testCreatePlayerWithInvalidPasswordTooLong() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid password (too long)");
        Player player = TestDataGenerator.generatePlayerWithInvalidPasswordLong();
        logger.debug("Generated player with long password: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Password must contain latin letters and numbers (min 7 max 15 characters).", "Wrong error title for long password");
    }

    @Test(description = "Create player with invalid password (no numbers)")
    public void testCreatePlayerWithInvalidPasswordNoNumbers() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid password (no numbers)");
        Player player = TestDataGenerator.generatePlayerWithInvalidPasswordNoNumbers();
        logger.debug("Generated player with password without numbers: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Password must contain latin letters and numbers (min 7 max 15 characters).", "Wrong error title for password with no numbers");
    }

    @Test(description = "Create player with invalid password (no letters)")
    public void testCreatePlayerWithInvalidPasswordNoLetters() {
        System.out.println("Running " + this.getClass().getSimpleName() + " in thread " + Thread.currentThread().getName());
        logger.info("Testing player creation with invalid password (no letters)");
        Player player = TestDataGenerator.generatePlayerWithInvalidPasswordNoLetters();
        logger.debug("Generated player with password without letters: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Password must contain latin letters and numbers (min 7 max 15 characters).", "Wrong error title for password with no letters");
    }

    // ==================== ROLE VALIDATION TESTS ====================

    @Test(description = "Create player with invalid role")
    public void testCreatePlayerWithInvalidRole() {
        logger.info("Testing player creation with invalid role");
        Player player = TestDataGenerator.generatePlayerWithInvalidRole();
        logger.debug("Generated player with invalid role: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "User can be created only with one role from the list: 'admin' or 'user'.", "Wrong error title for invalid role");
    }

    // ==================== REQUIRED FIELDS TESTS ====================

    @Test(description = "Create player with missing age")
    public void testCreatePlayerWithMissingAge() {
        logger.info("Testing player creation with missing age");
        Player player = TestDataGenerator.generatePlayerWithMissingAge();
        logger.debug("Generated player with missing age: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Required fields missing.", "Wrong error title for missing age");
    }

    @Test(description = "Create player with missing gender")
    public void testCreatePlayerWithMissingGender() {
        logger.info("Testing player creation with missing gender");
        Player player = TestDataGenerator.generatePlayerWithMissingGender();
        logger.debug("Generated player with missing gender: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Required fields missing.", "Wrong error title for missing gender");
    }

    @Test(description = "Create player with missing login")
    public void testCreatePlayerWithMissingLogin() {
        logger.info("Testing player creation with missing login");
        Player player = TestDataGenerator.generatePlayerWithMissingLogin();
        logger.debug("Generated player with missing login: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Required fields missing.", "Wrong error title for missing login");
    }

    @Test(description = "Create player with missing role")
    public void testCreatePlayerWithMissingRole() {
        logger.info("Testing player creation with missing role");
        Player player = TestDataGenerator.generatePlayerWithMissingRole();
        logger.debug("Generated player with missing role: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Required fields missing.", "Wrong error title for missing role");
    }

    @Test(description = "Create player with missing screenName")
    public void testCreatePlayerWithMissingScreenName() {
        logger.info("Testing player creation with missing screenName");
        Player player = TestDataGenerator.generatePlayerWithMissingScreenName();
        logger.debug("Generated player with missing screenName: {}", player);
        
        assertCreatePlayerWithValidationError(player, 400, "Required fields missing.", "Wrong error title for missing screenName");
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test(description = "Create player with non-existent editor")
    public void testCreatePlayerWithNonExistentEditor() {
        logger.info("Testing player creation with non-existent editor");
        Player player = TestDataGenerator.generateValidPlayer();
        logger.debug("Generated valid player: {}", player);
        
        assertCreatePlayerWithAuthorizationError(player, "nonexistent_user", 403, "Non-existent editor.", "Wrong error title for non-existent editor");
    }

    // ==================== HELPER METHODS ====================

    @Step("Create and verify player as {editor}")
    private PlayerResponse createAndVerifyPlayer(Player player, String editor) {
        logger.debug("Creating player as editor: {}", editor);
        ResponseWrapper<PlayerResponse> response = apiClient.createPlayer(editor, player);
        assertEquals(response.getResponse().statusCode(), 200, "Couldn't create player.");
        PlayerResponse body = response.readEntity();
        assertNotNull(body, "Response body should not be null");
        Integer id = body.getPlayerId();
        assertNotNull(id, "PlayerId should not be null");
        createdPlayerIds.add(id);
        logger.debug("Successfully created player with ID: {}", id);
        return body;
    }

    @Step("Assert player creation with validation error - expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertCreatePlayerWithValidationError(Player player, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting validation error for player creation with status: {}", expectedStatus);
        ResponseWrapper<?> response = apiClient.createPlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), player);
        assertEquals(response.getResponse().statusCode(), expectedStatus, "User should not be created.");
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Validation error confirmed: {}", expectedErrorTitle);
    }

    @Step("Assert player creation with authorization error - editor: {editor}, expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertCreatePlayerWithAuthorizationError(Player player, String editor, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting authorization error for player creation with editor: {}", editor);
        ResponseWrapper<?> response = apiClient.createPlayer(editor, player);
        assertEquals(response.getResponse().statusCode(), expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Authorization error confirmed: {}", expectedErrorTitle);
    }

    @Step("Assert player equals expected values")
    private void assertPlayerEquals(PlayerResponse actual, Player expected, SoftAssert sa) {
        logger.debug("Asserting player data matches expected values");
        sa.assertEquals(actual.getAge(), expected.getAge(), "Wrong age");
        sa.assertEquals(actual.getGender(), expected.getGender(), "Wrong gender");
        sa.assertEquals(actual.getLogin(), expected.getLogin(), "Wrong login");
        sa.assertEquals(actual.getPassword(), expected.getPassword(), "Wrong password");
        sa.assertEquals(actual.getRole(), expected.getRole(), "Wrong role");
        sa.assertEquals(actual.getScreenName(), expected.getScreenName(), "Wrong screen_name");
        sa.assertAll();
        logger.debug("Player data validation completed successfully");
    }
}
