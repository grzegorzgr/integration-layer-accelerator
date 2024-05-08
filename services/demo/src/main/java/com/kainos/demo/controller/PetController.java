package com.kainos.demo.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.demo.service.PetService;
import com.kainos.kafka.KafkaProducer;
import com.kainos.pets.api.model.CreatePetResponse;
import com.kainos.pets.api.model.Pet;
import com.kainos.pets.api.model.PetRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pets")
public class PetController {

    @Value("${kafka.topics.internalPets}")
    private String kafkaTopicsInternalPets;

    @Autowired
    public PetService petService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @ResponseStatus(OK)
    @GetMapping
    public List<Pet> listPets() {
        return petService.getPets();
    }

    @ResponseStatus(CREATED)
    @ResponseBody
    @PostMapping
    public CreatePetResponse createPets(@Valid @RequestBody PetRequest petRequest) {
        return petService.createPet(petRequest);
    }

    @ResponseStatus(ACCEPTED)
    @PostMapping("/async")
    public void createPetsAsync(@Valid @RequestBody PetRequest petRequest) {
        com.kainos.pets.avro.PetRequest petRequestAvro = com.kainos.pets.avro.PetRequest.newBuilder()
            .setName(petRequest.getName())
            .setTag(petRequest.getTag())
            .build();

        kafkaProducer.send(kafkaTopicsInternalPets, petRequest.getName(), petRequestAvro);
    }
}
