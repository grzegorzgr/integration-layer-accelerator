package tests.steps;

import static tests.model.TestDataKeys.CREATE_PET_RESPONSE;
import static tests.model.TestDataKeys.PET_REQUEST;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.PetRequest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import tests.clients.PetStoreClient;
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
        Response response = petStoreClient.getPets(traceId);
        petStoreValidator.validateNewPetIsAdded(response, petRequest, createPetResponse);
    }
}