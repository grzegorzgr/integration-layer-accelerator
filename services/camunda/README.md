# Spring Boot Applications with the Embedded Camunda Engine

## Run
- run application
- login to the Camunda web applications Cockpit running on `http://localhost:8080/` with the credentials specified in `application.yml` file:

```yaml
camunda:
  bpm:
    admin-user:
      id: admin
      password: admin
```
- go to `Cockpit -> Dashboard -> Processes` to observe running process instances

## Defining BPMN flow
- to view the `Orders` BPMN flow upload the `order_process.bpmn` file into BPMN online Viewer/Editor: https://bpmn.io/
- Camunda Modeler: https://camunda.com/download/modeler/

