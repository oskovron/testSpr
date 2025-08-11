package api;

import api.client.ResponseWrapper;
import api.model.error.ErrorBody;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.model.response.PlayersResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import util.TestDataGenerator;

import java.util.concurrent.CopyOnWriteArrayList;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PlayerGetTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerGetTest.class);
    private Player player;
    private PlayerResponse createdPlayer;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerGetTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        player = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating test player: {}", player);
        createdPlayer = createAndVerifyPlayer(player, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
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

    // ==================== GET PLAYER TESTS ====================

    @Test(description = "Get all players")
    public void testGetAllPlayers() {
        logger.info("Testing get all players");
        ResponseWrapper<PlayersResponse> response = apiClient.getAllPlayers();
        response.expectingStatusCode(200);
        PlayersResponse players = response.readEntity();
        assertNotNull(players, "Response should not be null");
        logger.debug("Retrieved players response: {}", players);
        response.getResponse().then().assertThat().body(matchesJsonSchemaInClasspath("schemas/players-schema.json"));
        logger.info("Successfully retrieved all players and validated schema");
    }

    @Test(description = "Get player by non-existent ID")
    public void testGetPlayerByNonExistentId() {
        logger.info("Testing get player by non-existent ID");
        ResponseWrapper<PlayerResponse> response = apiClient.getPlayer(999999);
        response.expectingStatusCode(404);
        ErrorBody error = response.readError(ErrorBody.class);
        assertEquals(error.getTitle(), "User does not exist.", "Wrong error title for non-existent user");
        logger.info("Successfully confirmed error for non-existent player: {}", error.getTitle());
    }

    @Test(description = "Get player with null playerId")
    public void testGetPlayerWithNullId() {
        logger.info("Testing get player with null playerId");
        ResponseWrapper<PlayerResponse> response = apiClient.getPlayer(null);
        response.expectingStatusCode(400);
        logger.info("Successfully confirmed 400 status for null playerId");
    }

    @Test(description = "Get player after creation and verify all fields")
    public void testGetPlayerAfterCreation() {
        logger.info("Testing get player after creation with field verification");
        // Get the player and verify all fields
        logger.debug("Retrieving created player for field verification");
        ResponseWrapper<PlayerResponse> response = apiClient.getPlayer(createdPlayer.getPlayerId());
        response.expectingStatusCode(200);
        PlayerResponse retrievedPlayer = response.readEntity();
        logger.debug("Retrieved player data: {}", retrievedPlayer);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(retrievedPlayer.getPlayerId(), createdPlayer.getPlayerId(), "Player ID should match");
        softAssert.assertEquals(retrievedPlayer.getAge(), player.getAge(), "Age should match");
        softAssert.assertEquals(retrievedPlayer.getGender(), player.getGender(), "Gender should match");
        softAssert.assertEquals(retrievedPlayer.getLogin(), player.getLogin(), "Login should match");
        softAssert.assertEquals(retrievedPlayer.getPassword(), player.getPassword(), "Password should match");
        softAssert.assertEquals(retrievedPlayer.getRole(), player.getRole(), "Role should match");
        softAssert.assertEquals(retrievedPlayer.getScreenName(), player.getScreenName(), "ScreenName should match");
        softAssert.assertAll();
        logger.info("Successfully verified all player fields after creation");
    }

    @Test(description = "Get player with boundary ID values")
    public void testGetPlayerWithBoundaryIds() {
        logger.info("Testing get player with boundary ID values");

        // Test with ID 1 (likely supervisor)
        logger.debug("Testing get player with ID 1");
        ResponseWrapper<PlayerResponse> response1 = apiClient.getPlayer(1);
        if (response1.getResponse().getStatusCode() == 200) {
            PlayerResponse player1 = response1.readEntity();
            logger.info("Successfully retrieved player with ID 1: {}", player1.getLogin());
        } else {
            logger.info("Player with ID 1 not found or access denied");
        }

        // Test with very large ID
        logger.debug("Testing get player with very large ID");
        ResponseWrapper<PlayerResponse> response2 = apiClient.getPlayer(Integer.MAX_VALUE);
        response2.expectingStatusCode(400);
        ErrorBody error = response2.readError(ErrorBody.class);
        assertEquals(error.getTitle(), "User does not exist.", "Wrong error title for very large ID");
        logger.info("Successfully confirmed error for very large player ID");
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
