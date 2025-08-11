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

public class PlayerCreateDuplicateNegativeTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateTest.class);
    private Player firstPlayer;
    private PlayerResponse createdPlayer;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerCreateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        firstPlayer = TestDataGenerator.generateValidPlayer("user");
        logger.debug("Creating test player: {}", firstPlayer);
        createdPlayer = createAndVerifyPlayer(firstPlayer, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdPlayer.getPlayerId());
        logger.info("Created first player with ID: {}", createdPlayer.getPlayerId());
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

    // ==================== UNIQUE CONSTRAINT TESTS ====================

    @Test(description = "Create player with duplicate login")
    public void testCreatePlayerWithDuplicateLogin() {
        logger.info("Testing player creation with duplicate login");
        Player duplicatePlayer = TestDataGenerator.generatePlayerWithDuplicateLogin(firstPlayer.getLogin());
        logger.debug("Attempting to create duplicate player with same login: {}", duplicatePlayer);
        assertCreatePlayerWithValidationError(duplicatePlayer, 403, "Login already exists.", "Wrong error title for duplicate login");
    }

    @Test(description = "Create player with duplicate screenName")
    public void testCreatePlayerWithDuplicateScreenName() {
        logger.info("Testing player creation with duplicate screenName");
        Player duplicatePlayer = TestDataGenerator.generatePlayerWithDuplicateScreenName(firstPlayer.getScreenName());
        logger.debug("Attempting to create duplicate player with same screenName: {}", duplicatePlayer);
        assertCreatePlayerWithValidationError(duplicatePlayer, 403, "Screen name already exists.", "Wrong error title for duplicate screenName");
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

    @Step("Assert player creation with validation error - expected status: {expectedStatus}, error title: {expectedErrorTitle}")
    private void assertCreatePlayerWithValidationError(Player player, int expectedStatus, String expectedErrorTitle, String assertionMessage) {
        logger.debug("Asserting validation error for player creation with status: {}", expectedStatus);
        ResponseWrapper<?> response = apiClient.createPlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), player);
        response.expectingStatusCode( expectedStatus);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), expectedErrorTitle, assertionMessage);
        logger.info("Validation error confirmed: {}", expectedErrorTitle);
    }
}
