package tests.clients.apiclients;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import tests.utils.TestSettings;

public class SfdcClient {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();
    private static final String CREATE_ACCOUNT = "/accounts/{accountName}";
    private static final String GET_CREATE_ACCOUNT_REQUESTS = "/requests/generic/createNewAccount/{traceId}";

    public void createAccount(String accountName, String traceId, int expectedStatusCode) {
        given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .when()
            .contentType(JSON)
            .pathParam("accountName", accountName)
            .post(CREATE_ACCOUNT)
            .then()
            .statusCode(expectedStatusCode);
    }

    public Response getCreateNewAccountRequests(String traceId) {
        return given()
            .baseUri(TEST_SETTINGS.getProperty("sfdc.stub_url"))
            .pathParam("traceId", traceId)
            .contentType(ContentType.JSON)
            .when()
            .get(GET_CREATE_ACCOUNT_REQUESTS)
            .then()
            .extract().response();
    }
}
