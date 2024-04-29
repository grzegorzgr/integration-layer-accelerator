package com.kainos.demo.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.ACCEPTED;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.demo.vendors.petstore.client.PetstoreClient;
import com.kainos.petstore.model.Pet;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetstoreClient petstoreClient;

    @ResponseStatus(ACCEPTED)
    @GetMapping
    public List<Pet> listPets() {
        return petstoreClient.getPets();
    }

    @ResponseStatus(OK)
    @PostMapping
    public void createPets(@Valid @RequestBody Pet pet) {
        petstoreClient.createPets(pet);
    }
}
