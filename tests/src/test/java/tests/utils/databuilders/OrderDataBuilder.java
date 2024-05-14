package tests.utils.databuilders;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import com.kainos.orders.api.model.OrderRequest;

import tests.utils.EasyRandomUtils;

public class OrderDataBuilder {
    public static OrderRequest prepareOrderRequest() {
        EasyRandomParameters parameters = EasyRandomUtils.getEasyRandomParameters(1, 1, 2);
        EasyRandom easyRandom = new EasyRandom(parameters);
        OrderRequest orderRequest = easyRandom.nextObject(OrderRequest.class);
        return orderRequest;
    }
}
