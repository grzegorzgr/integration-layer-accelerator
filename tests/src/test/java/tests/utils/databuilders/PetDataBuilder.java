package tests.utils.databuilders;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import com.kainos.pets.api.model.PetRequest;
import tests.utils.EasyRandomUtils;

public class PetDataBuilder {
    public static PetRequest preparePetRequest() {
        EasyRandomParameters parameters = EasyRandomUtils.getEasyRandomParameters(1, 1, 2);
        EasyRandom easyRandom = new EasyRandom(parameters);
        PetRequest petRequest = easyRandom.nextObject(PetRequest.class);
        return petRequest;
    }
}
