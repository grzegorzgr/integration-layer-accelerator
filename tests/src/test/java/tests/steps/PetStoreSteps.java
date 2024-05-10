package tests.steps;


import static org.awaitility.Awaitility.await;

import static tests.model.TestDataKeys.CREATE_PET_RESPONSE;
import static tests.model.TestDataKeys.GET_PETS_RESPONSE;
import static tests.model.TestDataKeys.PET_REQUEST;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.PetRequest;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import tests.clients.KafkaConsumerClient;
import tests.clients.PetStoreClient;
import tests.utils.TestDataSerenity;
import tests.utils.databuilders.PetDataBuilder;
import tests.validators.KafkaValidator;
import tests.validators.PetStoreValidator;

public class PetStoreSteps {
    private final PetStoreClient petStoreClient = new PetStoreClient();
    private final PetStoreValidator petStoreValidator = new PetStoreValidator();
    private final KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient();
    private final KafkaValidator kafkaValidator = new KafkaValidator();

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

    @And("message is sent to {} kafka topic")
    public void messageIsSentToInternalPetsKafkaTopic(String kafkaTopic) throws JsonProcessingException, JSONException {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        Response response = TestDataSerenity.get(GET_PETS_RESPONSE, Response.class);
        List<com.kainos.petstore.avro.Pet> petListMsgs = kafkaConsumerClient.getAllMsgsByTraceId(kafkaTopic, com.kainos.petstore.avro.Pet.getClassSchema(), traceId);
        kafkaValidator.validatePetEventOnKafka(response, petListMsgs);
    }

    @When("add new pet async endpoint is called and gets {int}")
    public void addNewPetAsyncEndpointIsCalledAndGets(int expectedStatusCode) {
        PetRequest petRequest = TestDataSerenity.get(PET_REQUEST, PetRequest.class);
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        petStoreClient.addPetAsync(petRequest, traceId, expectedStatusCode);
    }
}