package tests.clients.apiclients;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import org.springframework.http.HttpStatus;

import com.kainos.orders.api.model.OrderRequest;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.PetRequest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import tests.model.RequestFailure;
import tests.utils.TestSettings;

public class PetStoreClient {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();
    private static final String ADD_PET_ASYNC = "/pets/async";
    private static final String ADD_PET = "/pets";
    private static final String GET_PETS = "/pets";
    private static final String GET_CREATE_PETS_REQUESTS = "/requests/createPets/{traceId}";
    private static final String PET_STUB_FAIL_NEXT_CALL_URL = "/requests/fail";

    public void addPetAsync(PetRequest petRequest, String traceId, int expectedStatusCode) {
        given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .when()
            .contentType(JSON)
            .body(petRequest)
            .post(ADD_PET_ASYNC)
            .then()
            .statusCode(expectedStatusCode);
    }

    public CreatePetResponse addPet(PetRequest petRequest, String traceId, int expectedStatusCode) {
        return given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .when()
            .contentType(JSON)
            .body(petRequest)
            .post(ADD_PET)
            .then()
            .statusCode(expectedStatusCode)
            .extract().response().as(CreatePetResponse.class);
    }

    public Response getPets(String traceId) {
        return given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .contentType(ContentType.JSON)
            .when()
            .get(GET_PETS)
            .then()
            .extract().response();
    }

    public Response getCreatePetsRequests(String traceId) {
        return given()
            .baseUri(TEST_SETTINGS.getProperty("petstore.stub_url"))
            .pathParam("traceId", traceId)
            .contentType(ContentType.JSON)
            .when()
            .get(GET_CREATE_PETS_REQUESTS)
            .then()
            .extract().response();
    }

    public void instructPetStubToFail(RequestFailure failRequest) {
        given()
            .baseUri(TEST_SETTINGS.getProperty("petstore.stub_url"))
            .contentType(ContentType.JSON)
            .body(failRequest)
            .when()
            .post(PET_STUB_FAIL_NEXT_CALL_URL)
            .then()
            .statusCode(HttpStatus.OK.value());
    }
}