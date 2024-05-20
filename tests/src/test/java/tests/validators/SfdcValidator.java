package tests.validators;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfdc.account.model.Account;

import io.restassured.response.Response;
import tests.utils.ObjectMapperBuilder;

public class SfdcValidator {
    private final ObjectMapper mapper = ObjectMapperBuilder.build();

    public void validateAccountCreated(Response response, Account account) throws JsonProcessingException {
        List<Account> createdNewAccounts = mapper.readValue(
            response.asString(), new TypeReference<>() {
            });

        assertEquals(1, createdNewAccounts.size());
        assertEquals(account.getName(), createdNewAccounts.get(0).getName());
    }
}
