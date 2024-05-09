Feature: PetStore

  Scenario: Add new pet
    Given add new pet request is prepared
    When add new pet endpoint is called and gets 201
    Then new pet is added