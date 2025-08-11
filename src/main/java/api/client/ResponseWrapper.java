package api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class ResponseWrapper<T> {

    private final Response response;
    private final Class<T> responseClass;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
    private static final Logger logger = LoggerFactory.getLogger(ResponseWrapper.class);

    public ResponseWrapper(Response response, Class<T> responseClass) {
        this.response = response;
        this.responseClass = responseClass;
    }

    public Response getResponse() {
        if (response == null) {
            throw new IllegalStateException("Response is null");
        }
        return response;
    }

    public T readEntity() {
        String body = response.getBody().asString();
        if (body == null || body.isEmpty()) {
            throw new AssertionError("Response body is empty; cannot map to " + responseClass.getSimpleName());
        }
        try {
            logger.debug("Deserializing response to {}", responseClass.getSimpleName());
            return OBJECT_MAPPER.readValue(body, responseClass);
        } catch (Exception e) {
            throw new AssertionError("Failed to deserialize response to " + responseClass.getSimpleName() +
                    ": " + body, e);
        }
    }

    public ResponseWrapper<T> expectingStatusCode(int statusCode) {
        int actual = response.getStatusCode();
        Assert.assertEquals(actual,statusCode, "Expected HTTP status code " + statusCode + ", but was " + actual);
        return this;
    }

    public <E> E readError(Class<E> errorClass) {
        String body = response.getBody().asString();
        if (body == null || body.isEmpty()) {
            throw new AssertionError("Response body is empty; cannot map to " + errorClass.getSimpleName());
        }

        try {
            logger.debug("Deserializing error body to {}", errorClass.getSimpleName());
            return OBJECT_MAPPER.readValue(body, errorClass);
        } catch (Exception e) {
            throw new AssertionError("Failed to parse error body to " + errorClass.getSimpleName()
                    + ". Raw response: " + body, e);
        }
    }

}
