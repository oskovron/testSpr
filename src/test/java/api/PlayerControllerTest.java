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
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.requests.PlayerApiClient;
import base.BaseTest;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import util.TestDataGenerator;

/**
 * Integration tests for Player Controller
 * This class contains end-to-end workflow tests that span multiple operations
 * Specific functionality tests are in dedicated classes:
 * - PlayerCreateTest: All create-related tests
 * - PlayerGetTest: All get/retrieval tests  
 * - PlayerUpdateTest: All update-related tests
 * - PlayerDeleteTest: All delete-related tests
 * - PlayerAuthorizationTest: All authorization and role-based tests
 */
public class PlayerControllerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PlayerControllerTest.class);
    private PlayerApiClient apiClient;
    private List<Integer> createdPlayerIds;

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up PlayerControllerTest");
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

    // ==================== INTEGRATION WORKFLOW TESTS ====================

    @Test(description = "Complete player lifecycle: Create -> Get -> Update -> Delete")
    public void testCompletePlayerLifecycle() {
        logger.info("Testing complete player lifecycle: Create -> Get -> Update -> Delete");
        
        // 1. Create player
        Player player = TestDataGenerator.generateValidPlayer();
        logger.debug("Generated test player: {}", player);
        PlayerResponse createdPlayer = createAndVerifyPlayer(player, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        logger.info("Step 1: Created player with ID: {}", createdPlayer.getPlayerId());
        
        // 2. Get player and verify data
        logger.debug("Step 2: Retrieving created player for verification");
        ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(createdPlayer.getPlayerId());
        getResponse.expectingStatusCode(200);
        PlayerResponse retrievedPlayer = getResponse.readEntity();
        logger.debug("Retrieved player data: {}", retrievedPlayer);
        assertPlayerEquals(retrievedPlayer, player, new SoftAssert());
        logger.info("Step 2: Successfully retrieved and validated player data");
        
        // 3. Update player
        Player updateData = new Player();
        updateData.setAge(30);
        updateData.setGender("female");
        logger.debug("Step 3: Updating player with age={}, gender={}", updateData.getAge(), updateData.getGender());
        
        ResponseWrapper<PlayerResponse> updateResponse = apiClient.updatePlayer(
            ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
            createdPlayer.getPlayerId(), 
            updateData
        );
        updateResponse.expectingStatusCode(200);
        logger.info("Step 3: Successfully updated player");
        
        // 4. Verify update
        logger.debug("Step 4: Verifying player updates");
        ResponseWrapper<PlayerResponse> getAfterUpdateResponse = apiClient.getPlayer(createdPlayer.getPlayerId());
        getAfterUpdateResponse.expectingStatusCode(200);
        PlayerResponse updatedPlayer = getAfterUpdateResponse.readEntity();
        assertEquals(updatedPlayer.getAge(), 30, "Age should be updated to 30");
        assertEquals(updatedPlayer.getGender(), "female", "Gender should be updated to female");
        logger.info("Step 4: Successfully verified player updates");
        
        // 5. Delete player
        logger.debug("Step 5: Deleting player");
        apiClient.deletePlayer(ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(), createdPlayer.getPlayerId());
        logger.info("Step 5: Successfully deleted player");
        
        // 6. Verify deletion
        logger.debug("Step 6: Verifying player deletion");
        ResponseWrapper<PlayerResponse> getAfterDeleteResponse = apiClient.getPlayer(createdPlayer.getPlayerId());
        getAfterDeleteResponse.expectingStatusCode(404);
        logger.info("Step 6: Successfully verified player deletion");
        
        logger.info("Complete player lifecycle test passed successfully");
    }

    @Test(description = "Multi-user workflow: Create multiple users and verify isolation")
    public void testMultiUserWorkflow() {
        logger.info("Testing multi-user workflow: Create multiple users and verify isolation");
        
        // Create multiple users with different roles
        Player user1 = TestDataGenerator.generateValidPlayer("user");
        Player user2 = TestDataGenerator.generateValidPlayer("user");
        Player admin1 = TestDataGenerator.generateValidPlayer("admin");
        
        logger.debug("Creating first user: {}", user1);
        PlayerResponse createdUser1 = createAndVerifyPlayer(user1, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        logger.info("Created first user with ID: {}", createdUser1.getPlayerId());
        
        logger.debug("Creating second user: {}", user2);
        PlayerResponse createdUser2 = createAndVerifyPlayer(user2, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        logger.info("Created second user with ID: {}", createdUser2.getPlayerId());
        
        logger.debug("Creating admin: {}", admin1);
        PlayerResponse createdAdmin1 = createAndVerifyPlayer(admin1, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        logger.info("Created admin with ID: {}", createdAdmin1.getPlayerId());
        
        // Verify each user can be retrieved independently
        logger.debug("Verifying first user data isolation");
        ResponseWrapper<PlayerResponse> getUser1Response = apiClient.getPlayer(createdUser1.getPlayerId());
        getUser1Response.expectingStatusCode(200);
        PlayerResponse retrievedUser1 = getUser1Response.readEntity();
        assertPlayerEquals(retrievedUser1, user1, new SoftAssert());
        logger.info("Successfully verified first user data isolation");
        
        logger.debug("Verifying second user data isolation");
        ResponseWrapper<PlayerResponse> getUser2Response = apiClient.getPlayer(createdUser2.getPlayerId());
        getUser2Response.expectingStatusCode(200);
        PlayerResponse retrievedUser2 = getUser2Response.readEntity();
        assertPlayerEquals(retrievedUser2, user2, new SoftAssert());
        logger.info("Successfully verified second user data isolation");
        
        logger.debug("Verifying admin data isolation");
        ResponseWrapper<PlayerResponse> getAdmin1Response = apiClient.getPlayer(createdAdmin1.getPlayerId());
        getAdmin1Response.expectingStatusCode(200);
        PlayerResponse retrievedAdmin1 = getAdmin1Response.readEntity();
        assertPlayerEquals(retrievedAdmin1, admin1, new SoftAssert());
        logger.info("Successfully verified admin data isolation");
        
        // Verify data isolation - each user has unique data
        logger.debug("Verifying data uniqueness across users");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotEquals(createdUser1.getLogin(), createdUser2.getLogin(), "Users should have different logins");
        softAssert.assertNotEquals(createdUser1.getScreenName(), createdUser2.getScreenName(), "Users should have different screen names");
        softAssert.assertNotEquals(createdUser1.getLogin(), createdAdmin1.getLogin(), "User and admin should have different logins");
        softAssert.assertAll();
        logger.info("Successfully verified data uniqueness across users");
    }

    @Test(description = "Data consistency workflow: Verify data integrity across operations")
    public void testDataConsistencyWorkflow() {
        logger.info("Testing data consistency workflow: Verify data integrity across operations");
        
        // Create a player with specific data
        Player player = TestDataGenerator.generateValidPlayer();
        logger.debug("Creating test player: {}", player);
        PlayerResponse createdPlayer = createAndVerifyPlayer(player, ConfigFactoryProvider.apiConfig().defaultSupervisorLogin());
        logger.info("Created test player with ID: {}", createdPlayer.getPlayerId());
        
        // Perform multiple operations and verify data consistency
        for (int i = 0; i < 3; i++) {
            logger.debug("Iteration {}: Testing data consistency", i + 1);
            
            // Get player
            logger.debug("Retrieving player for consistency check");
            ResponseWrapper<PlayerResponse> getResponse = apiClient.getPlayer(createdPlayer.getPlayerId());
            getResponse.expectingStatusCode(200);
            PlayerResponse retrievedPlayer = getResponse.readEntity();
            logger.debug("Retrieved player data: {}", retrievedPlayer);
            
            // Verify core data remains consistent
            logger.debug("Verifying core data consistency");
            assertEquals(retrievedPlayer.getLogin(), player.getLogin(), "Login should remain consistent");
            assertEquals(retrievedPlayer.getRole(), player.getRole(), "Role should remain consistent");
            assertEquals(retrievedPlayer.getScreenName(), player.getScreenName(), "ScreenName should remain consistent");
            logger.info("Iteration {}: Core data consistency verified", i + 1);
            
            // Update age
            Player updateData = new Player();
            updateData.setAge(20 + i);
            logger.debug("Updating player age to: {}", updateData.getAge());
            
            ResponseWrapper<PlayerResponse> updateResponse = apiClient.updatePlayer(
                ConfigFactoryProvider.apiConfig().defaultSupervisorLogin(),
                createdPlayer.getPlayerId(), 
                updateData
            );
            updateResponse.expectingStatusCode(200);
            logger.info("Iteration {}: Successfully updated player age", i + 1);
            
            // Verify update
            logger.debug("Verifying age update");
            ResponseWrapper<PlayerResponse> getAfterUpdateResponse = apiClient.getPlayer(createdPlayer.getPlayerId());
            getAfterUpdateResponse.expectingStatusCode(200);
            PlayerResponse updatedPlayer = getAfterUpdateResponse.readEntity();
            assertEquals(updatedPlayer.getAge(), 20 + i, "Age should be updated correctly");
            logger.info("Iteration {}: Age update verified", i + 1);
        }
        
        logger.info("Data consistency workflow test completed successfully");
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
