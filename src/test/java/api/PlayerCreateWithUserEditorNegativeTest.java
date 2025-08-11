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

public class PlayerCreateWithUserEditorNegativeTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateTest.class);
    private Player playerUser;
    private PlayerResponse createdPlayer;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerCreateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        playerUser = TestDataGenerator.generateValidPlayer("user");
        logger.debug("Creating test player: {}", playerUser);
        createdPlayer = createAndVerifyPlayer(playerUser, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdPlayer.getPlayerId());
        logger.info("Created test player with ID: {}", createdPlayer.getPlayerId());
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

    // ==================== AUTHORIZATION TESTS ====================
    @Test(description = "Create player with user editor (unauthorized)")
    public void testCreatePlayerWithUserEditor() {
        logger.info("Testing player creation with user editor (unauthorized)");
        // Try to create another player using the user as editor
        Player newPlayer = TestDataGenerator.generateValidPlayer();
        logger.debug("Attempting to create new player with user editor: {}", newPlayer);
        assertCreatePlayerWithAuthorizationError(newPlayer, playerUser.getLogin(), 403, "Only those with role 'supervisor' or 'admin' can create users.", "Wrong error title for user editor creation");
    }

    @Test(description = "User cannot access admin operations")
    public void testUserCannotAccessAdminOperations() {
        logger.info("Testing user cannot access admin operations");
        // Try to create an admin using user as editor
        Player adminPlayer = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Attempting to create admin with user editor: {}", adminPlayer);

        assertCreatePlayerWithAuthorizationError(adminPlayer, playerUser.getLogin(), 403,"Only those with role 'supervisor' or 'admin' can create users.", "Wrong error title for user creating admin");
    }

    // ==================== HELPER METHODS ====================

    @Step("Create and verify player as {editor}")
    private PlayerResponse createAndVerifyPlayer(Player player, String editor) {
        logger.debug("Creating player as editor: {}", editor);
        ResponseWrapper<PlayerResponse> response = apiClient.createPlayer(editor, player);
        response.expectingStatusCode(200);
        PlayerResponse body = response.readEntity();
        assertNotNull(body, "Response body should not be null");
        Integer id = body.getPlayerId();
        assertNotNull(id, "PlayerId should not be null");
        createdPlayerIds.add(id);
        logger.debug("Successfully created player with ID: {}", id);
        return body;
    }

    @Step("Assert player creation with authorization error - editor: {editor}, expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertCreatePlayerWithAuthorizationError(Player player, String editor, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting authorization error for player creation with editor: {}", editor);
        ResponseWrapper<?> response = apiClient.createPlayer(editor, player);
        response.expectingStatusCode(expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Authorization error confirmed: {}", expectedErrorTitle);
    }
}
