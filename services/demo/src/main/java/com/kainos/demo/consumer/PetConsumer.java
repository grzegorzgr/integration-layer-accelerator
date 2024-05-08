package com.kainos.demo.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.kainos.demo.service.PetService;
import com.kainos.kafka.KafkaProducer;
import com.kainos.mapper.ModelMapperFactory;
import com.kainos.pets.api.model.PetRequest;

@Component
public class PetConsumer {
    private static final String CONSUMER_GROUP_ID = "pets";
    private static final ModelMapper MODEL_MAPPER = ModelMapperFactory.getMapper();

    @Value("${kafka.topics.pets}")
    private String pets;

    @Autowired
    private PetService petService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @KafkaListener(topics = "${kafka.topics.internalPets}", clientIdPrefix = CONSUMER_GROUP_ID, id = CONSUMER_GROUP_ID)
    public void listen(ConsumerRecord<String, com.kainos.pets.avro.PetRequest> record) {
        PetRequest petRequest = MODEL_MAPPER.map(record, PetRequest.class);
        Long createdPetId = petService.createPet(petRequest).getId();
        com.kainos.petstore.avro.Pet petAvro = com.kainos.petstore.avro.Pet.newBuilder()
            .setId(createdPetId)
            .setName(petRequest.getName())
            .setTag(petRequest.getTag())
            .build();
        kafkaProducer.send(pets, createdPetId.toString(), petAvro);
    }
}
