package tests.steps;

import static org.awaitility.Awaitility.await;

import static tests.model.TestDataKeys.CREATE_ORDER_RESPONSE;
import static tests.model.TestDataKeys.GET_PETS_RESPONSE;
import static tests.model.TestDataKeys.ORDER_REQUEST;
import static tests.model.TestDataKeys.TRACE_ID;

import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kainos.orders.api.model.CreateOrderResponse;
import com.kainos.orders.api.model.OrderRequest;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import tests.clients.KafkaConsumerClient;
import tests.clients.apiclients.OrderClient;
import tests.clients.apiclients.PetStoreClient;
import tests.utils.TestDataSerenity;
import tests.validators.KafkaValidator;
import tests.validators.PetStoreValidator;

public class KafkaSteps {
    private final KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient();
    private final KafkaValidator kafkaValidator = new KafkaValidator();
    @And("message is sent to {} kafka topic")
    public void messageIsSentToKafkaTopic(String kafkaTopic) throws JsonProcessingException, JSONException {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        Response response = TestDataSerenity.get(GET_PETS_RESPONSE, Response.class);
        List<com.kainos.petstore.avro.Pet> petListMsgs = kafkaConsumerClient.getAllMsgsByTraceId(kafkaTopic, com.kainos.petstore.avro.Pet.getClassSchema(), traceId);
        kafkaValidator.validatePetEventOnKafka(response, petListMsgs);

    }
    @And("no message is sent to {} kafka topic")
    public void noMessageIsSentToPetsKafkaTopic(String kafkaTopic) {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        List<com.kainos.petstore.avro.Pet> petListMsgs = kafkaConsumerClient.getAllMsgsByTraceId(kafkaTopic,
            com.kainos.petstore.avro.Pet.getClassSchema(), traceId);
        kafkaValidator.kafkaMessageListIsEmpty(petListMsgs);
    }

    @Then("All consumers are resumed")
    public void allConsumersAreResumed() {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        kafkaConsumerClient.sendResumeAllConsumersRequest(traceId);
        await().until(kafkaValidator.allConsumersAreUp(kafkaConsumerClient.getConsumerPausedStatuses(traceId)));
    }

    @Then("order message is sent to {} kafka topic")
    public void orderMessageIsSentToTopicKafkaTopic(String kafkaTopic) {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        List<com.kainos.orders.avro.Order> orderMsgs = kafkaConsumerClient.getAllMsgsByTraceId(kafkaTopic,
            com.kainos.orders.avro.Order.getClassSchema(), traceId);
        CreateOrderResponse createOrderResponse = TestDataSerenity.get(CREATE_ORDER_RESPONSE, CreateOrderResponse.class);
        OrderRequest orderRequest = TestDataSerenity.get(ORDER_REQUEST, OrderRequest.class);
        kafkaValidator.validateOrderEventOnKafka(orderMsgs, createOrderResponse, orderRequest);

    }
}