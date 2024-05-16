# IL accelerator

Generic helm chart capable of rendering any number of applications dynamically (as long as they are defined in values-apps.yaml)

In order to render the template please use the following command:

```helm template --namespace xyz --set global.tag=xyz-123 -f ./ops/k8s/helm/application/values-common.yaml -f ./ops/k8s/helm/application/values-apps.yaml ./ops/k8s/helm/application```

Chart can easily be migrated between potential self contained systems allowing deployment of just a group of microservices. This will ensure proper versioning for a specific set of microservices without any need of rerolling entire namespace during upgrades.

For installing the chart run the following command (with optional --dry-run to allow kubernetes to validate rendered yamls)
Subsitute namespace name, tag and application name (in test scenario it' 'custom-app' ) with any that fit your use case.

```helm upgrade --install --namespace xyz --set global.tag=xyz-123 -f ./ops/k8s/helm/application/values-common.yaml -f ./ops/k8s/helm/application/values-apps.yaml custom-app ./ops/k8s/helm/application (--dry-run)```

## application services:
- api-gateway
- camunda
- demo
- error-handling

## application external system stubs:
- petstore
- sfdc

## exposed HTTP endpoints via `api-gateway` service:
- POST `/pets` (`demo` service -> `petstore` stub)
- POST `/pets/async` (`demo` service -> `internalPets` Kafka topic -> `petstore` stub + `pets` Kafka topic)
- GET `/pets` (`demo` service -> `petstore` stub)
- POST `/accounts/{accountName}` (`demo` service -> `sfdc` stub)
- POST `/orders` (`camunda` service -> `orders` Kafka topic)
- GET `/operations-service/paused-consumers` (`error-handling` service -> ETCD + Kafka)
- POST `/operations-service/paused-consumers/resume` (`error-handling` service -> ETCD + Kafka)

please check the attached Postman collection under `/postman-collection` directory

## local usage
Run the `start-local-env.sh` script under `/ops/local` directory