package api.client;

import static io.restassured.RestAssured.given;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class RestClient {
    protected static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    protected Configuration configuration;
    private RequestSpecification requestSpecification;
    private RestAssuredConfig restAssuredConfig = RestAssured.config()
            .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails());

    protected abstract Configuration defaultConfiguration();

    public RestClient() {
        getSession();
    }

    private void getSession() {
        configuration = defaultConfiguration();
        logger.info("Initializing RestClient with baseUri={}, contentType={}",
                configuration.getServicePath(), configuration.getContentType());
        // Ensure default parser is JSON for all API clients
        RestAssured.defaultParser = Parser.JSON;
        requestSpecification = new RequestSpecBuilder()
                .setConfig(restAssuredConfig)
                .setBaseUri(configuration.getServicePath())
                .setContentType(configuration.getContentType())
                .log(io.restassured.filter.log.LogDetail.ALL)
                .addFilter(new AllureRestAssured())
                .build();
    }

    public <F> ResponseWrapper<F> get(String path, Class<F> responseClass) {
        logger.info("HTTP GET {}", path);
        Response response = given()
                .spec(requestSpecification)
                .get(path);
        logger.info("HTTP GET {} -> {} ({} ms)", path, response.getStatusCode(), response.getTime());
        return new ResponseWrapper<>(response, responseClass);
    }

    public <F> ResponseWrapper<F> get(String path, String pathParamName, Object pathParamValue, Map<String, Object> queryParams, Class<F> responseClass) {
        logger.info("HTTP GET {}", path);
        logger.debug("Path param: {}={}, query params: {}", pathParamName, pathParamValue, queryParams);
        Response response = given()
                .spec(requestSpecification)
                .pathParam(pathParamName, pathParamValue)
                .queryParams(queryParams)
                .get(path);
        logger.info("HTTP GET {} -> {} ({} ms)", path, response.getStatusCode(), response.getTime());
        response.then().log().all();
        return new ResponseWrapper<>(response, responseClass);
    }

    protected <T, F> ResponseWrapper<F> post(String path, T payload, Class<F> responseClass) {
        logger.info("HTTP POST {}", path);
        logger.debug("Payload: {}", payload);
        Response response = given()
                .spec(requestSpecification)
                .body(payload)
                .post(path);
        logger.info("HTTP POST {} -> {} ({} ms)", path, response.getStatusCode(), response.getTime());
        response.then().log().all();
        return new ResponseWrapper<>(response, responseClass);
    }

    protected <T, F> ResponseWrapper<F> patch(String path, Map<String, Object> pathParams, T payload, Class<F> responseClass) {
        logger.info("HTTP PATCH {}", path);
        logger.debug("Path params: {}, payload: {}", pathParams, payload);
        Response response = given()
                .spec(requestSpecification)
                .pathParams(pathParams)
                .body(payload)
                .patch(path);
        logger.info("HTTP PATCH {} -> {} ({} ms)", path, response.getStatusCode(), response.getTime());
        return new ResponseWrapper<>(response, responseClass);
    }

    protected <T> Response delete(String path, Map<String, Object> pathParam, T payload) {
        logger.info("HTTP DELETE {}", path);
        logger.debug("Path params: {}, payload: {}", pathParam, payload);
        Response response = given()
                .spec(requestSpecification)
                .pathParams(pathParam)
                .body(payload)
                .delete(path);
        logger.info("HTTP DELETE {} -> {} ({} ms)", path, response.getStatusCode(), response.getTime());
        response.then().log().all();
        return response;
    }
}
