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