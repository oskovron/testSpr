package api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import api.client.ResponseWrapper;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import util.TestDataGenerator;

public class PlayerDeleteTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerDeleteTest.class);
    private Player playerUser, adminPlayer;
    private PlayerResponse createdPlayerUser, createdAdmin;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerDeleteTest");
        apiClient = new PlayerApiClient();
        createdPlayerIds = new CopyOnWriteArrayList<>();

        playerUser = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating test player: {}", playerUser);
        createdPlayerUser = createAndVerifyPlayer(playerUser, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdPlayerUser.getPlayerId());
        logger.info("Created test player with ID: {}", createdPlayerUser.getPlayerId());

        adminPlayer = TestDataGenerator.generateValidPlayer("admin");
        logger.debug("Creating admin for deletion test: {}", adminPlayer);
        createdAdmin = createAndVerifyPlayer(adminPlayer, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        createdPlayerIds.add(createdAdmin.getPlayerId());
        logger.info("Created admin with ID: {} for deletion test", createdAdmin.getPlayerId());
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

    // ==================== DELETE PLAYER TESTS ====================

    @Test(description = "Delete player with supervisor")
    public void testDeletePlayerWithSupervisor() {
        logger.info("Testing delete player with supervisor");
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), createdPlayerUser.getPlayerId());
        assertEquals(response.getStatusCode(), 204, "Expected status code 204 for successful deletion");
        logger.info("Successfully deleted player with supervisor");

        // Verify player is deleted
        verifyPlayerDeletion(createdPlayerUser.getPlayerId());
    }

    @Test(description = "Delete player with admin")
    public void testDeletePlayerWithAdmin() {
        logger.info("Testing delete player with admin");
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultAdminLogin(), createdPlayerUser.getPlayerId());
        assertEquals(response.getStatusCode(), 204, "Expected status code 204 for successful deletion");
        logger.info("Successfully deleted player with admin");

        // Verify player is deleted
        verifyPlayerDeletion(createdPlayerUser.getPlayerId());
    }

    @Test(description = "Delete admin by supervisor")
    public void testDeleteAdminBySupervisor() {
        logger.info("Testing delete admin by supervisor");
        // Supervisor should be able to delete admin
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), createdAdmin.getPlayerId());
        assertEquals(response.getStatusCode(), 204, "Supervisor should be able to delete admin");
        logger.info("Successfully deleted admin by supervisor");

        // Verify admin is deleted
        verifyPlayerDeletion(createdAdmin.getPlayerId());
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test(description = "Delete non-existent player")
    public void testDeleteNonExistentPlayer() {
        logger.info("Testing delete non-existent player");
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), 999999);
        assertEquals(response.getStatusCode(), 404, "Status code should be 404 when deleting non-existent player");
        logger.info("Successfully confirmed 404 status for non-existent player deletion");
    }

    @Test(description = "Delete player with non-existent editor")
    public void testDeletePlayerWithNonExistentEditor() {
        logger.info("Testing delete player with non-existent editor");
        Response response = apiClient.deletePlayer("nonexistent_editor", createdPlayerUser.getPlayerId());
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when deleting with non-existent editor");
        logger.info("Successfully confirmed 403 status for non-existent editor deletion");
    }

    @Test(description = "Delete supervisor (should be forbidden)")
    public void testDeleteSupervisor() {
        logger.info("Testing delete supervisor (should be forbidden)");
        // Try to delete supervisor (assuming supervisor has ID 1)
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), 1);
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when deleting supervisor");
        logger.info("Successfully confirmed 403 status for supervisor deletion");
    }

    @Test(description = "Delete admin by admin (self-deletion)")
    public void testDeleteAdminByAdmin() {
        logger.info("Testing delete admin by admin (self-deletion)");
        // Try to delete admin by himself
        Response response = apiClient.deletePlayer(createdAdmin.getLogin(), createdAdmin.getPlayerId());
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when admin tries to delete himself");
        logger.info("Successfully confirmed 403 status for admin self-deletion");
    }

    @Test(description = "Delete user by user (self-deletion)")
    public void testDeleteUserByUser() {
        logger.info("Testing delete user by user (self-deletion)");
        // Try to delete user by himself
        Response response = apiClient.deletePlayer(playerUser.getLogin(), createdPlayerUser.getPlayerId());
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when user tries to delete himself");
        logger.info("Successfully confirmed 403 status for user self-deletion");
    }

    @Test(description = "User cannot delete admin")
    public void testUserCannotDeleteAdmin() {
        logger.info("Testing user cannot delete admin");
        // Try to delete admin using user as editor
        Response response = apiClient.deletePlayer(playerUser.getLogin(), createdAdmin.getPlayerId());
        assertEquals(response.getStatusCode(), 403, "Status code should be 403 when user tries to delete admin");
        logger.info("Successfully confirmed 403 status for user deleting admin");
    }

    @Test(description = "Admin can delete other users")
    public void testAdminCanDeleteOtherUsers() {
        logger.info("Testing admin can delete other users");
        // Admin should be able to delete the user
        Response response = apiClient.deletePlayer(createdAdmin.getLogin(), createdPlayerUser.getPlayerId());
        assertEquals(response.getStatusCode(), 204, "Admin should be able to delete other users");
        logger.info("Successfully deleted user by admin");

        // Verify user is deleted
        verifyPlayerDeletion(createdPlayerUser.getPlayerId());
    }

    @Test(description = "Delete player with boundary ID values")
    public void testDeletePlayerWithBoundaryIds() {
        logger.info("Testing delete player with boundary ID values");
        
        // Test with very large ID
        logger.debug("Testing delete player with very large ID");
        Response response = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), Integer.MAX_VALUE);
        assertEquals(response.getStatusCode(), 400, "Status code should be 404 when deleting player with very large ID");
        logger.info("Successfully confirmed 404 status for very large player ID deletion");
        
        // Test with negative ID
        logger.debug("Testing delete player with negative ID");
        Response response2 = apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), -1);
        assertEquals(response2.getStatusCode(), 400, "Status code should be 404 when deleting player with negative ID");
        logger.info("Successfully confirmed 404 status for negative player ID deletion");
    }

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

    // ==================== HELPER METHODS ====================

    @Step("Verify player deletion - playerId: {playerId}")
    private void verifyPlayerDeletion(Integer playerId) {
        logger.debug("Verifying player deletion for ID: {}", playerId);
        ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(playerId);
        assertEquals(getResponse.getResponse().statusCode(), 404, "Expected status code 404 for not found user.");
        logger.info("Successfully confirmed player deletion for ID: {}", playerId);
    }
}
