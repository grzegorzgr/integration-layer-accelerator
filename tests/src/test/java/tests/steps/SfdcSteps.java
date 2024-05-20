package tests.steps;

import static org.awaitility.Awaitility.await;

import static tests.model.TestDataKeys.ACCOUNT;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sfdc.account.model.Account;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import tests.clients.apiclients.SfdcClient;
import tests.utils.TestDataSerenity;
import tests.utils.databuilders.AccountDataBuilder;
import tests.validators.SfdcValidator;

public class SfdcSteps {

    SfdcClient sfdcClient = new SfdcClient();
    SfdcValidator sfdcValidator = new SfdcValidator();

    @Given("new account is prepared")
    public void newAccountIsPrepared() {
        traceId();
        Account account = AccountDataBuilder.prepareAccount();
        TestDataSerenity.set(ACCOUNT, account);
    }

    @When("create new account request is sent and gets {int}")
    public void createNewAccountRequestIsSentAndGets(int expectedStatusCode) {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        Account account = TestDataSerenity.get(ACCOUNT, Account.class);
        sfdcClient.createAccount(account.getName(), traceId, expectedStatusCode);
    }

    @Then("new account is created")
    public void newAccountIsCreated() throws JsonProcessingException {
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        Account account = TestDataSerenity.get(ACCOUNT, Account.class);
        Response response = await().until(() ->  sfdcClient.getCreateNewAccountRequests(traceId),
            res -> res.asString().contains(account.getName()));;
        sfdcValidator.validateAccountCreated(response, account);
    }
}
