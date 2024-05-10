package tests.validators;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.Pet;
import com.kainos.pets.api.model.PetRequest;

import io.restassured.response.Response;
import tests.utils.ObjectMapperBuilder;

public class PetStoreValidator {

    private final ObjectMapper mapper = ObjectMapperBuilder.build();

    public void validateNewPetIsAdded(Response response, PetRequest petRequest, CreatePetResponse createPetResponse,
        Response getCreatePetsRequests) throws JsonProcessingException {
        List<Pet> petList = mapper.readValue(
            response.asString(), new TypeReference<>() {
            });

        List<com.kainos.petstore.model.Pet> createPetRequestsList = mapper.readValue(
            getCreatePetsRequests.asString(), new TypeReference<>() {
            });

        assertEquals(1, petList.size());
        if (createPetResponse == null) {
            assertEquals(createPetRequestsList.get(0).getId(), petList.get(0).getId());
        } else {
            assertEquals(createPetResponse.getId(), petList.get(0).getId());
        }
        assertEquals(petRequest.getName(), petList.get(0).getName());
        assertEquals(petRequest.getTag(), petList.get(0).getTag());
    }
}
