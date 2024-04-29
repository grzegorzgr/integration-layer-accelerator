package com.kainos.demo.vendors.petstore.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kainos.petstore.model.Pet;

@FeignClient(value = "petstore", url = "${petstore.url}", configuration = PetstoreClientConfiguration.class)
public interface PetstoreClient {

    String PETS_PATH = "/pets";

    @RequestMapping(method = RequestMethod.GET, value = PETS_PATH)
    List<Pet> getPets();

    @RequestMapping(method = RequestMethod.POST, value = PETS_PATH)
    void createPets(@RequestBody Pet pet);
}