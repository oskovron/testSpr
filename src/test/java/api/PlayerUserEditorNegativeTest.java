package api;

import api.client.ResponseWrapper;
import api.model.error.ErrorBody;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import util.TestDataGenerator;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PlayerUserEditorNegativeTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerUpdateTest.class);
    private Player userPlayer, targetPlayer;
    private PlayerResponse createdUser, createdTarget;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerUpdateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        userPlayer = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating test player: {}", userPlayer);
        createdUser = createAndVerifyPlayer(userPlayer,
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdUser.getPlayerId());
        logger.info("Created test player with ID: {}", createdUser.getPlayerId());

        targetPlayer = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating target player: {}", targetPlayer);
        createdTarget = createAndVerifyPlayer(targetPlayer,
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdTarget.getPlayerId());
        logger.info("Created target player with ID: {}", createdTarget.getPlayerId());
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

    @Test(description = "Update player with user editor (unauthorized)")
    public void testUpdatePlayerWithUserEditor() {
        logger.info("Testing update player with user editor (unauthorized)");
        // Try to update target player using user as editor
        Player updateData = new Player();
        updateData.setAge(25);
        logger.debug("Attempting to update target player with user editor");

        assertUpdatePlayerWithAuthorizationError(createdTarget.getPlayerId(),
                updateData,
                createdUser.getLogin(),
                403,
                "Only those with role 'supervisor' or 'admin' can update users.",
                "Wrong error title for user editor update");
    }

    @Test(description = "User cannot delete other users")
    public void testUserCannotDeleteOtherUsers() {
        logger.info("Testing user cannot delete other users");
        Response response = apiClient.deletePlayer(createdUser.getLogin(), createdTarget.getPlayerId());
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when user tries to delete another user");
        logger.info("Successfully confirmed 403 status for user deleting another user");
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
