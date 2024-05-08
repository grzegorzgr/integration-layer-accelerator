Feature: PetStore

  Scenario: Add new pet
    Given new pet is prepared
    When add new pet endpoint is called and gets 200
    Then new pet is added