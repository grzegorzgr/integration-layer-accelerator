package tests.steps;

import static org.awaitility.Awaitility.await;

import static tests.model.TestDataKeys.CREATE_PET_RESPONSE;
import static tests.model.TestDataKeys.GET_PETS_RESPONSE;
import static tests.model.TestDataKeys.PET_REQUEST;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.PetRequest;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import tests.clients.apiclients.PetStoreClient;
import tests.model.RequestFailure;
import tests.utils.TestDataSerenity;
import tests.utils.databuilders.PetDataBuilder;
import tests.validators.PetStoreValidator;

public class PetStoreSteps {
    private final PetStoreClient petStoreClient = new PetStoreClient();
    private final PetStoreValidator petStoreValidator = new PetStoreValidator();

    @Given("add new pet request is prepared")
    public void newPetIsPrepared() {
        PetRequest petRequest = PetDataBuilder.preparePetRequest();
        traceId();
        TestDataSerenity.set(PET_REQUEST, petRequest);
    }

    @When("add new pet endpoint is called and gets {int}")
    public void newPetEndpointIsCalledAndGets(int expectedStatusCode) {
        PetRequest petRequest = TestDataSerenity.get(PET_REQUEST, PetRequest.class);
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        CreatePetResponse createPetResponse = petStoreClient.addPet(petRequest, traceId, expectedStatusCode);
        TestDataSerenity.set(CREATE_PET_RESPONSE, createPetResponse);
    }

    @Then("new pet is added")
    public void newPetIsAdded() throws JsonProcessingException {

        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        PetRequest petRequest = TestDataSerenity.get(PET_REQUEST, PetRequest.class);
        CreatePetResponse createPetResponse = TestDataSerenity.get(CREATE_PET_RESPONSE, CreatePetResponse.class);
        Response getCreatePetsRequests = await().until(() -> petStoreClient.getCreatePetsRequests(traceId),
            res -> res.asString().contains(petRequest.getName()));
        Response response = await().until(() -> petStoreClient.getPets(traceId),
            c -> c.getStatusCode() == HttpStatus.SC_OK);
        TestDataSerenity.set(GET_PETS_RESPONSE, response);
        petStoreValidator.validateNewPetIsAdded(response, petRequest, createPetResponse, getCreatePetsRequests);
    }

    @Then("new pet is not added")
    public void newPetIsNotAdded() {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        await().until(() -> petStoreClient.getCreatePetsRequests(traceId),
            res -> res.statusCode() == 404);
    }

    @When("add new pet async endpoint is called and gets {int}")
    public void addNewPetAsyncEndpointIsCalledAndGets(int expectedStatusCode) {
        PetRequest petRequest = TestDataSerenity.get(PET_REQUEST, PetRequest.class);
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        petStoreClient.addPetAsync(petRequest, traceId, expectedStatusCode);
    }

    @And("pet stub is instructed to fail on {string} call and respond {int}")
    public void petStubIsInstructedToFailOnCallAndRespond(String method, int httpStatusCode) {
        RequestFailure failRequestCaps = RequestFailure.builder()
            .requestName(method)
            .statusCode(httpStatusCode)
            .traceId(TestDataSerenity.traceId())
            .build();
        petStoreClient.instructPetStubToFail(failRequestCaps);
    }
}