package tests.steps;

import static tests.model.TestDataKeys.PET;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kainos.petstore.model.Pet;

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

    @Given("new pet is prepared")
    public void newPetIsPrepared() {
        Pet pet = PetDataBuilder.preparePet();
        TestDataSerenity.set(TRACE_ID, traceId());
        TestDataSerenity.set(PET, pet);
    }

    @When("add new pet endpoint is called and gets {int}")
    public void newPetEndpointIsCalledAndGets(int expectedStatusCode) {
        Pet pet = TestDataSerenity.get(PET, Pet.class);
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        petStoreClient.addPet(pet, traceId, expectedStatusCode);
    }

    @Then("new pet is added")
    public void newPetIsAdded() throws JsonProcessingException {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        Pet pet = TestDataSerenity.get(PET, Pet.class);
        Response response = petStoreClient.getPets(traceId);
        petStoreValidator.validateNewPetIsAdded(response, pet);
    }
}
