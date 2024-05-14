package tests.clients.apiclients;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import com.kainos.orders.api.model.CreateOrderResponse;
import com.kainos.orders.api.model.OrderRequest;

import io.restassured.response.Response;
import tests.utils.TestSettings;

public class OrderClient {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();
    private static final String ADD_ORDER = "/orders";

    public CreateOrderResponse addOrderRequest(OrderRequest orderRequest, String traceId, int expectedStatusCode) {
        return given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .when()
            .contentType(JSON)
            .body(orderRequest)
            .post(ADD_ORDER)
            .then()
            .statusCode(expectedStatusCode)
            .extract().response().as(CreateOrderResponse.class);
    }
}