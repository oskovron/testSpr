package api;

import api.client.ResponseWrapper;
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

public class PlayerAdminEditorTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PlayerUpdateTest.class);
    private Player admin1, admin2;
    private PlayerResponse createdAdmin1, createdAdmin2;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerUpdateTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        admin1 = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Creating first admin: {}", admin1);
        createdAdmin1 = createAndVerifyPlayer(admin1, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdAdmin1.getPlayerId());
        logger.info("Created first admin with ID: {}", createdAdmin1.getPlayerId());

        admin2 = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Creating second admin: {}", admin2);
        createdAdmin2 = createAndVerifyPlayer(admin2, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdAdmin2.getPlayerId());
        logger.info("Created second admin with ID: {}", createdAdmin2.getPlayerId());
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


    @Test(description = "Admin can update other admins")
    public void testAdminCanUpdateOtherAdmins() {
        logger.info("Testing admin can update other admins");
        // Admin1 should be able to update Admin2
        Player updateData = new Player();
        updateData.setAge(40);
        logger.debug("First admin updating second admin age to: {}", updateData.getAge());

        ResponseWrapper<PlayerResponse> response = apiClient.updatePlayer(
                createdAdmin1.getLogin(),
                createdAdmin2.getPlayerId(),
                updateData);
        response.expectingStatusCode( 200);
        logger.info("Successfully updated admin by another admin");

        // Verify the update
        verifyPlayerFieldUpdate(createdAdmin2.getPlayerId(), "age", 40);
    }

    @Test(description = "Admin can delete other admins")
    public void testAdminCanDeleteOtherAdmins() {
        logger.info("Testing admin can delete other admins");
        // Admin1 should be able to delete Admin2
        Response response = apiClient.deletePlayer(createdAdmin1.getLogin(), createdAdmin2.getPlayerId());
        assertEquals(response.getStatusCode(), 204, "Admin should be able to delete other admins");
        logger.info("Successfully deleted admin by another admin");

        // Verify admin is deleted
        verifyPlayerDeletion(createdAdmin2.getPlayerId());
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

    @Step("Verify player deletion - playerId: {playerId}")
    private void verifyPlayerDeletion(Integer playerId) {
        logger.debug("Verifying player deletion for ID: {}", playerId);
        ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(playerId);
        getResponse.expectingStatusCode(404);
        logger.info("Successfully confirmed player deletion for ID: {}", playerId);
    }
}
