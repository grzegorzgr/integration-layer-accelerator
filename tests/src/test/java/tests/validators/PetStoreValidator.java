package tests.validators;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.petstore.model.Pet;

import io.restassured.response.Response;
import tests.utils.ObjectMapperBuilder;

public class PetStoreValidator {

    private final ObjectMapper mapper = ObjectMapperBuilder.build();

    public void validateNewPetIsAdded(Response response, Pet pet) throws JsonProcessingException {
        List<Pet> petList = mapper.readValue(
            response.asString(), new TypeReference<>() {
            });

        assertEquals(1, petList.size());
        assertEquals(pet.getId(), petList.get(0).getId());
        assertEquals(pet.getName(), petList.get(0).getName());
        assertEquals(pet.getTag(), petList.get(0).getTag());
    }
}
