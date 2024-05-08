package tests.utils.databuilders;

import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import com.kainos.petstore.model.Pet;

import tests.utils.EasyRandomUtils;

public class PetDataBuilder {
    public static Pet preparePet() {
        EasyRandomParameters parameters = EasyRandomUtils.getEasyRandomParameters(1, 1, 2);
        EasyRandom easyRandom = new EasyRandom(parameters);
        Pet pet = easyRandom.nextObject(Pet.class);
        pet.setId(Long.valueOf(RandomStringUtils.randomNumeric(8)));
        return pet;
    }
}
