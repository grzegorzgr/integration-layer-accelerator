package com.kainos.demo.service;

import java.util.List;
import java.util.Random;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kainos.demo.vendors.petstore.client.PetstoreClient;
import com.kainos.mapper.ModelMapperFactory;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.Pet;
import com.kainos.pets.api.model.PetRequest;

@Service
public class PetService {
    private static final ModelMapper MODEL_MAPPER = ModelMapperFactory.getMapper();

    @Autowired
    private PetstoreClient petstoreClient;

    public List<Pet> getPets() {
        return petstoreClient.getPets().stream()
            .map(pet -> MODEL_MAPPER.map(pet, Pet.class))
            .toList();
    }

    public CreatePetResponse createPet(PetRequest petRequest) {
        Long id = generateId();
        com.kainos.petstore.model.Pet pet = MODEL_MAPPER.map(petRequest, com.kainos.petstore.model.Pet.class);
        pet.setId(id);
        petstoreClient.createPets(pet);
        return new CreatePetResponse().id(id);
    }

    private Long generateId() {
        return new Random().nextLong();
    }
}
