#!/bin/bash

NAMESPACE=$1

API_GATEWAY_URL=http://api-gateway-svc.${NAMESPACE}.svc:8080
KAFKA_KEY_DESERIALIZER=org.apache.kafka.common.serialization.StringDeserializer
KAFKA_VALUE_DESERIALIZERr=io.confluent.kafka.serializers.KafkaAvroDeserializer
KAFKA_AUTO_OFFSET_RESET=earliest
KAFKA_BROKER=kafka-main-headless.${NAMESPACE}.svc:9092
KAFKA_SCHEMA_REGISTRY=http://schema-registry-svc.${NAMESPACE}.svc:8081
PETSTORE_STUB_URL=http://petstore-stub-svc.${NAMESPACE}.svc:8080
