
## How to run e2e test

e2e tests can be found in **helm-accelerator/tests** and are written using **Serenity with Cucumber BDD**. 
Following plugins need to be installed:
- Cucumber For java
- Gherkin

Test structure:
1. Features - includes feature files with Gherkin plain text test/scenario description
2. Steps - includes step definition classes, where Gherkin steps are connected with Java methods
3. Clients - includes classes with methods for a selected client
4. Validators - includes classes with methods with assertions

Parallelization is done using Junit. The settings for parallel execution is in junit-platform.properties file.
Here's the command for parallel execution:
```./gradlew -Dcucumber.filter.tags="@tag" -Dcucumber.execution.parallel.enabled=true clean test aggregate```

A single test can also be run by clicking RMB on scenario line in feature file and selecting Run or Debug.
In order to run the tests configuration template has to be set in *Run -> Edit Configurations*. 
In *Edit Configurations* click on *Edit configuration template* and set the values as follows:

1. **Main class** - net.serenitybdd.cucumber.cli.Main
2. **Glue** - net.serenitybdd.cucumber.actors tests.steps