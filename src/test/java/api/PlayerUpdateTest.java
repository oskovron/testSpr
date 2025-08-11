package api;

import api.client.ResponseWrapper;
import api.model.error.ErrorBody;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import util.TestDataGenerator;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PlayerUpdateTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerUpdateTest.class);
    private Player playerUser;
    private PlayerResponse createdPlayerUser;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerUpdateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        playerUser = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating test player: {}", playerUser);
        createdPlayerUser = createAndVerifyPlayer(playerUser, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdPlayerUser.getPlayerId());
        logger.info("Created test player with ID: {}", createdPlayerUser.getPlayerId());
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

    // ==================== UPDATE PLAYER TESTS ====================

    @Test(description = "Update player age")
    public void testUpdatePlayerAge() {
        logger.info("Testing update player age");
        Player updateData = new Player();
        updateData.setAge(30);
        logger.debug("Updating player age to: {}", updateData.getAge());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player age");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "age", 30);
    }

    @Test(description = "Update player gender")
    public void testUpdatePlayerGender() {
        logger.info("Testing update player gender");
        Player updateData = new Player();
        updateData.setGender("female");
        logger.debug("Updating player gender to: {}", updateData.getGender());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player gender");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "gender", "female");
    }

    @Test(description = "Update player login")
    public void testUpdatePlayerLogin() {
        logger.info("Testing update player login");
        Player updateData = TestDataGenerator.generateUpdatePlayerWithNewLogin();
        logger.debug("Updating player login to: {}", updateData.getLogin());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player login");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "login", updateData.getLogin());
    }

    @Test(description = "Update player screenName")
    public void testUpdatePlayerScreenName() {
        logger.info("Testing update player screenName");
        Player updateData = TestDataGenerator.generateUpdatePlayerWithNewScreenName();
        logger.debug("Updating player screenName to: {}", updateData.getScreenName());
        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player screenName");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "screenName", updateData.getScreenName());
    }

    @Test(description = "Update player password")
    public void testUpdatePlayerPassword() {
        logger.info("Testing update player password");
        Player updateData = TestDataGenerator.generateUpdatePlayerWithNewPassword();
        logger.debug("Updating player password to: {}", updateData.getPassword());
        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player password");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "password", updateData.getPassword());
    }

    @Test(description = "Update multiple fields at once")
    public void testUpdateMultipleFields() {
        logger.info("Testing update multiple fields at once");
        Player updateData = new Player();
        updateData.setAge(35);
        updateData.setGender("female");
        updateData.setLogin("updatedLogin123");
        logger.debug("Updating multiple fields: age={}, gender={}, login={}", updateData.getAge(), updateData.getGender(), updateData.getLogin());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated multiple fields");

        // Verify the updates
        ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(createdPlayerUser.getPlayerId());
        getResponse.expectingStatusCode(200);
        assertEquals(response.getResponse().statusCode(), 200, "User not found.");
        PlayerResponse updatedPlayer = getResponse.readEntity();
        assertEquals(updatedPlayer.getAge(), 35, "Age should be updated to 35");
        assertEquals(updatedPlayer.getGender(), "female", "Gender should be updated to female");
        assertEquals(updatedPlayer.getLogin(), "updatedLogin123", "Login should be updated");
        logger.info("Successfully verified all updated fields");
    }

    // ==================== VALIDATION TESTS ====================

    @Test(description = "Update player with invalid age")
    public void testUpdatePlayerWithInvalidAge() {
        logger.info("Testing update player with invalid age");
        Player updateData = new Player();
        updateData.setAge(15); // Too young
        logger.debug("Attempting to update player age to invalid value: {}", updateData.getAge());

        assertUpdatePlayerWithValidationError(createdPlayerUser.getPlayerId(), updateData, 403, "User should be older than 16 and younger than 60 years old.", "Wrong error title for invalid age on update");
    }

    @Test(description = "Update player with invalid gender")
    public void testUpdatePlayerWithInvalidGender() {
        logger.info("Testing update player with invalid gender");
        Player updateData = new Player();
        updateData.setGender("invalid_gender");
        logger.debug("Attempting to update player gender to invalid value: {}", updateData.getGender());

        assertUpdatePlayerWithValidationError(createdPlayerUser.getPlayerId(), updateData, 400, "Gender can be male/female.", "Wrong error title for invalid gender on update");
    }

    @Test(description = "Update player with invalid password")
    public void testUpdatePlayerWithInvalidPassword() {
        logger.info("Testing update player with invalid password");
        Player updateData = new Player();
        updateData.setPassword("short"); // Too short
        logger.debug("Attempting to update player password to invalid value: {}", updateData.getPassword());

        assertUpdatePlayerWithValidationError(createdPlayerUser.getPlayerId(), updateData, 400, "Password must contain latin letters and numbers (min 7 max 15 characters).", "Wrong error title for invalid password on update");
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test(description = "Update non-existent player")
    public void testUpdateNonExistentPlayer() {
        logger.info("Testing update non-existent player");
        Player updateData = new Player();
        updateData.setAge(25);
        logger.debug("Attempting to update non-existent player with age: {}", updateData.getAge());

        assertUpdatePlayerWithAuthorizationError(999999, updateData, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), 404, "User does not exist.", "Wrong error title for non-existent user on update");
    }

    @Test(description = "Update player with non-existent editor")
    public void testUpdatePlayerWithNonExistentEditor() {
        logger.info("Testing update player with non-existent editor");
        Player updateData = new Player();
        updateData.setAge(25);
        logger.debug("Attempting to update player with non-existent editor");

        assertUpdatePlayerWithAuthorizationError(createdPlayerUser.getPlayerId(), updateData, "nonexistent_editor", 403, "Non-existent editor.", "Wrong error title for non-existent editor on update");
    }

    @Test(description = "User can update their own profile")
    public void testUserCanUpdateOwnProfile() {
        logger.info("Testing user can update their own profile");

        // User should be able to update their own profile
        Player updateData = new Player();
        updateData.setAge(30);
        logger.debug("User updating their own age to: {}", updateData.getAge());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                createdPlayerUser.getLogin(), createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated user's own profile");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "age", 30);
    }

    @Test(description = "Update player with admin editor")
    public void testUpdatePlayerWithAdminEditor() {
        logger.info("Testing update player with admin editor");
        Player updateData = new Player();
        updateData.setAge(40);
        logger.debug("Updating player age to {} using admin editor", updateData.getAge());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultAdminLogin(),
                createdPlayerUser.getPlayerId(), updateData);
        response.expectingStatusCode(200);
        logger.info("Successfully updated player using admin editor");

        // Verify the update
        verifyPlayerFieldUpdate(createdPlayerUser.getPlayerId(), "age", 40);
    }

    // ==================== HELPER METHODS ====================

    @Step("Create and verify player as {editor}")
    private PlayerResponse createAndVerifyPlayer(Player player, String editor) {
        logger.debug("Creating player as editor: {}", editor);
        ResponseWrapper<PlayerResponse> response = apiClient.createPlayer(editor, player);
        assertEquals(response.getResponse().statusCode(), 200, "User should be created.");
        PlayerResponse body = response.readEntity();
        assertNotNull(body, "Response body should not be null");
        Integer id = body.getPlayerId();
        assertNotNull(id, "PlayerId should not be null");
        createdPlayerIds.add(id);
        logger.debug("Successfully created player with ID: {}", id);
        return body;
    }

    @Step("Verify player field update - field: {fieldName}, expected value: {expectedValue}")
    private void verifyPlayerFieldUpdate(Integer playerId, String fieldName, Object expectedValue) {
        logger.debug("Verifying field update: {} = {}", fieldName, expectedValue);
        ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(playerId);
        getResponse.expectingStatusCode(200);
        PlayerResponse updatedPlayer = getResponse.readEntity();

        switch (fieldName) {
            case "age":
                assertEquals(updatedPlayer.getAge(), expectedValue, "Age should be updated correctly");
                break;
            case "gender":
                assertEquals(updatedPlayer.getGender(), expectedValue, "Gender should be updated correctly");
                break;
            case "login":
                assertEquals(updatedPlayer.getLogin(), expectedValue, "Login should be updated correctly");
                break;
            case "password":
                assertEquals(updatedPlayer.getPassword(), expectedValue, "Password should be updated correctly");
                break;
            case "screenName":
                assertEquals(updatedPlayer.getScreenName(), expectedValue, "ScreenName should be updated correctly");
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
        logger.info("Successfully verified field update: {} = {}", fieldName, expectedValue);
    }

    @Step("Assert update player with validation error - expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertUpdatePlayerWithValidationError(Integer playerId, Player updateData, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting validation error for player update with status: {}", expectedStatus);
        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), playerId, updateData);
        response.expectingStatusCode(expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Validation error confirmed: {}", expectedErrorTitle);
    }

    @Step("Assert update player with authorization error - editor: {editor}, expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertUpdatePlayerWithAuthorizationError(Integer playerId, Player updateData, String editor, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting authorization error for player update with editor: {}", editor);
        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(editor, playerId, updateData);
        response.expectingStatusCode(expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Authorization error confirmed: {}", expectedErrorTitle);
    }
}
