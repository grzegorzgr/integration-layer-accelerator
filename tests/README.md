
## How to run e2e test

e2e tests can be found in **helm-accelerator/tests** and are written using **Serenity with Cucumber BDD**. 
Following plugins need to be installed:
- Cucumber for Java
- Gherkin

**Test structure:**
1. Features - includes feature files with Gherkin plain text test/scenario description
2. Steps - includes step definition classes, where Gherkin steps are connected with Java methods
3. Clients - includes classes with methods for a selected client
4. Validators - includes classes with methods with assertions

Parallelization is done using Junit. The settings for parallel execution is in junit-platform.properties file.
Here's the command for parallel execution:
```./gradlew -Dcucumber.filter.tags="@tag" -Dcucumber.execution.parallel.enabled=true clean test aggregate```

**Tags**\
In order to tag a specific scenario - there should be added **@tag** right above **Scenario:/Scenario Outline:** of the test.
Below is part of the feature file *PetStore.feature*. If a specific scenario is to be tagged, the tag needs to be added directly 
above the line with **Scenario**, as in the example below:

@smoke\
**Scenario**: Add new pet - sync flow\
*Given* add new pet request is prepared\
*When* add new pet endpoint is called and gets 201\
*Then* new pet is added

Similarly, the whole feature can be tagged, however, then a tag should be added above the line with **Feature**.

@smoke\
**Feature**: PetStore

**Run Configurations in Intellij**:\
A single test can also be run by clicking RMB on scenario line in feature file and selecting Run or Debug.
In order to run the tests configuration template has to be set in *Run -> Edit Configurations*. 
In *Edit Configurations* click on *Edit configuration template* and set the values as follows:

1. **Main class** - net.serenitybdd.cucumber.cli.Main
2. **Glue** - net.serenitybdd.cucumber.actors tests.steps