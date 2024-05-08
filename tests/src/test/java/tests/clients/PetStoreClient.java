package tests.clients;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import org.springframework.http.HttpStatus;

import com.kainos.petstore.model.Pet;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import tests.utils.TestSettings;

public class PetStoreClient {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();
    private static final String ADD_PET = "/pets";
    private static final String GET_PETS = "/pets";

    public void addPet(Pet pet, String traceId, int expectedStatusCode) {
        given()
            .baseUri(TEST_SETTINGS.getProperty("api.gateway_url"))
            .header("trace-id", traceId)
            .when()
            .contentType(JSON)
            .body(pet)
            .post(ADD_PET)
            .then()
            .statusCode(expectedStatusCode);
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