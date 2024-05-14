Feature: PetStore

  Scenario: Add new pet - sync flow
    Given add new pet request is prepared
    When add new pet endpoint is called and gets 201
    Then new pet is added

  Scenario: Add new pet - async flow
    Given add new pet request is prepared
    When add new pet async endpoint is called and gets 202
    Then new pet is added
    And message is sent to pets kafka topic

  Scenario: Add new pet - Negative test with paused consumer
    Given add new pet request is prepared
    And pet stub is instructed to fail on "createPets" call and respond 500
    When add new pet async endpoint is called and gets 202
    Then new pet is not added
    And no message is sent to pets kafka topic
    Then All consumers are resumed
    Then new pet is added
    And message is sent to pets kafka topic

  Scenario: Camunda flow
    Given add new order request is prepared
    When order request is sent and gets 201
    Then order message is sent to orders kafka topic