package tests.validators;

import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

import java.util.List;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.pets.api.model.Pet;

import io.restassured.response.Response;
import tests.utils.ObjectMapperBuilder;

public class KafkaValidator {
    private final ObjectMapper mapper = ObjectMapperBuilder.build();

    public void validatePetEventOnKafka(Response response, List<com.kainos.petstore.avro.Pet> petListMsgs) throws JsonProcessingException, JSONException {
        List<Pet> petList = mapper.readValue(
            response.asString(), new TypeReference<>() {
            });

        String expected = mapper.writeValueAsString(petList.get(0));
        String actual = petListMsgs.get(0).toString();

        JSONAssert.assertEquals(expected, actual, STRICT);
    }
}
