*Generic helm chart capable of rendering any number of applications dynamically* (as long as they are defined in values-apps.yaml)

In order to render the template please use the following command:

```helm template --namespace xyz --set global.tag=xyz-123 -f ./ops/k8s/helm/application/values-common.yaml -f ./ops/k8s/helm/application/values-apps.yaml ./ops/k8s/helm/application```

Chart can easily be migrated between potential self contained systems allowing deployment of just a group of microservices. This will ensure proper versioning for a specific set of microservices without any need of rerolling entire namespace during upgrades.

For installing the chart run the following command (with optional --dry-run to allow kubernetes to validate rendered yamls)
Subsitute namespace name, tag and application name (in test scenario it' 'custom-app' ) with any that fit your use case.

```helm upgrade --install --namespace xyz --set global.tag=xyz-123 -f ./ops/k8s/helm/application/values-common.yaml -f ./ops/k8s/helm/application/values-apps.yaml custom-app ./ops/k8s/helm/application (--dry-run)```