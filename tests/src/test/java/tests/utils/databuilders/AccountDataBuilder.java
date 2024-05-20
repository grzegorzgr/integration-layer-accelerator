package tests.utils.databuilders;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import tests.model.Account;
import tests.utils.EasyRandomUtils;

public class AccountDataBuilder {

    public static Account prepareAccount() {
        EasyRandomParameters parameters = EasyRandomUtils.getEasyRandomParameters(1, 1, 2);
        EasyRandom easyRandom = new EasyRandom(parameters);
        Account account = easyRandom.nextObject(Account.class);
        return account;
    }
}
