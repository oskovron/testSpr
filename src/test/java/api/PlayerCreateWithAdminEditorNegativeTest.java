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

public class PlayerCreateWithAdminEditorNegativeTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateTest.class);
    private Player adminPlayer;
    private PlayerResponse createdAdmin;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerCreateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        adminPlayer = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Creating admin: {}", adminPlayer);
        createdAdmin = createAndVerifyPlayer(adminPlayer, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdAdmin.getPlayerId());
        logger.info("Created admin with ID: {}", createdAdmin.getPlayerId());
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

    @Test(description = "Admin cannot create supervisor")
    public void testAdminCannotCreateSupervisor() {
        logger.info("Testing admin cannot create supervisor");

        // Try to create a supervisor using admin as editor
        Player supervisorPlayer = TestDataGenerator.generateValidPlayer("supervisor");
        logger.debug("Attempting to create supervisor with admin editor: {}", supervisorPlayer);

        assertCreatePlayerWithAuthorizationError(
                supervisorPlayer,
                createdAdmin.getLogin(),
                403,
                "User can be created only with one role from the list: 'admin' or 'user'.",
                "Wrong error title for admin creating supervisor");
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
        response.expectingStatusCode( expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Authorization error confirmed: {}", expectedErrorTitle);
    }
}
