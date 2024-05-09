package tests.clients;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.PetRequest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import tests.utils.TestSettings;

public class PetStoreClient {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();
    private static final String ADD_PET = "/pets";
    private static final String GET_PETS = "/pets";

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
}