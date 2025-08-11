package api.requests;

import api.client.Configuration;
import api.client.ResponseWrapper;
import api.client.RestClient;
import api.model.request.Player;
import api.model.response.PlayerResponse;
import api.model.response.PlayersResponse;
import common.env.ConfigFactoryProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class PlayerApiClient extends RestClient {
    private static final Logger log = LoggerFactory.getLogger(PlayerApiClient.class);
    @Override
    protected Configuration defaultConfiguration() {
        return new Configuration(ConfigFactoryProvider.apiConfig().baseUrl(), "application/json");
    }

    @Step("Create player as {editor} with payload")
    public ResponseWrapper<PlayerResponse> createPlayer(String editor, Player player) {
        logger.info("Creating player with editor: {}, player: {}", editor, player);
        log.debug("Payload for create: {}", player);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("age", player.getAge());
        queryParams.put("gender", player.getGender());
        queryParams.put("login", player.getLogin());
        queryParams.put("password", player.getPassword());
        queryParams.put("role", player.getRole());
        queryParams.put("screenName", player.getScreenName());
        return get(ConfigFactoryProvider.apiConfig().endpointPlayerCreate(), "editor", editor, queryParams, PlayerResponse.class);
    }

    @Step("Get player by id {playerId}")
    public ResponseWrapper<PlayerResponse> getPlayer(Integer playerId) {
        logger.info("Getting player with ID: {}", playerId);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("playerId", playerId);
        return post(ConfigFactoryProvider.apiConfig().endpointPlayerGet(), requestBody, PlayerResponse.class);
    }

    @Step("Get all players")
    public ResponseWrapper<PlayersResponse> getAllPlayers() {
        logger.info("Getting all players");
        return get(ConfigFactoryProvider.apiConfig().endpointPlayerGetAll(), PlayersResponse.class);
    }

    @Step("Update player {playerId} as {editor}")
    public ResponseWrapper<PlayerResponse> updatePlayer(String editor, Integer playerId, Player updatePlayer) {
        logger.info("Updating player with editor: {}, playerId: {}, player: {}", editor, playerId, updatePlayer);
        log.debug("Update payload: {}", updatePlayer);

        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("editor", editor);
        pathParams.put("id", playerId);

        return patch(ConfigFactoryProvider.apiConfig().endpointPlayerUpdate(), pathParams, updatePlayer, PlayerResponse.class);
    }

    @Step("Delete player {playerId} as {editor}")
    public Response deletePlayer(String editor, Integer playerId) {
        logger.info("Deleting player with editor: {}, playerId: {}", editor, playerId);
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("editor", editor);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("playerId", playerId);

        return delete(ConfigFactoryProvider.apiConfig().endpointPlayerDelete(), pathParams, requestBody);
    }
}
