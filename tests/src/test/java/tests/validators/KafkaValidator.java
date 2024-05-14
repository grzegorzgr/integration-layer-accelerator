package tests.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.orders.api.model.CreateOrderResponse;
import com.kainos.orders.api.model.OrderRequest;
import com.kainos.pets.api.model.Pet;

import io.restassured.response.Response;
import tests.clients.KafkaConsumerClient;
import tests.utils.ObjectMapperBuilder;

public class KafkaValidator {
    private final ObjectMapper mapper = ObjectMapperBuilder.build();

    public void validatePetEventOnKafka(Response response, List<com.kainos.petstore.avro.Pet> petListMsgs) throws JsonProcessingException
        , JSONException {
        List<Pet> petList = mapper.readValue(
            response.asString(), new TypeReference<>() {
            });

        String expected = mapper.writeValueAsString(petList.get(0));
        String actual = petListMsgs.get(0).toString();

        JSONAssert.assertEquals(expected, actual, STRICT);
    }

    public void kafkaMessageListIsEmpty(List<com.kainos.petstore.avro.Pet> petListMsgs) {
        assertTrue(petListMsgs.isEmpty());
    }

    public Callable<Boolean> allConsumersAreUp(List<KafkaConsumerClient.ConsumersResponse> consumerResponse) {
        return consumerResponse::isEmpty;
    }

    public void validateOrderEventOnKafka(List<com.kainos.orders.avro.Order> orderMsgs, CreateOrderResponse createOrderResponse,
        OrderRequest orderRequest) {
        assertEquals(1, orderMsgs.size());
        assertEquals(createOrderResponse.getId(), orderMsgs.get(0).getId());
        assertEquals(orderRequest.getName(), orderMsgs.get(0).getName());
    }
}