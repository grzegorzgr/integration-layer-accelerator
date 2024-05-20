#!/bin/bash

NAMESPACE=$1

export API_GATEWAY_URL=http://api-gateway-svc.${NAMESPACE}.svc:8080
export KAFKA_BROKER=kafka-main-headless.${NAMESPACE}.svc:9092
export KAFKA_SCHEMA_REGISTRY=http://schema-registry-svc.${NAMESPACE}.svc:8081
export PETSTORE_STUB_URL=http://petstore-stub-svc.${NAMESPACE}.svc:8080
export SFDC_STUB_URL=http://sfdc-stub-svc.${NAMESPACE}.svc:8080
