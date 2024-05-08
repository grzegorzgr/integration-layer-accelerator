package tests.utils;

import java.util.Random;

import org.jeasy.random.EasyRandomParameters;

public class EasyRandomUtils {

    private static final int POOL_SIZE = 100;
    private static final Random RANDOM = new Random();

    public static EasyRandomParameters getEasyRandomParameters(int collectionSizeFrom, int collectionSizeTo, int randomizationDepth) {
        return new EasyRandomParameters()
            .seed(RANDOM.nextLong())
            .randomizationDepth(randomizationDepth)
            .collectionSizeRange(collectionSizeFrom, collectionSizeTo)
            .overrideDefaultInitialization(true)
            .objectPoolSize(POOL_SIZE);
    }

}
