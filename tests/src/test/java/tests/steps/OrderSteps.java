package tests.steps;

import static tests.model.TestDataKeys.CREATE_ORDER_RESPONSE;
import static tests.model.TestDataKeys.ORDER_REQUEST;
import static tests.model.TestDataKeys.TRACE_ID;
import static tests.utils.TestDataSerenity.traceId;

import java.util.List;

import com.kainos.orders.api.model.CreateOrderResponse;
import com.kainos.orders.api.model.OrderRequest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tests.clients.KafkaConsumerClient;
import tests.clients.apiclients.OrderClient;
import tests.utils.TestDataSerenity;
import tests.utils.databuilders.OrderDataBuilder;
import tests.validators.KafkaValidator;

public class OrderSteps {
    private final OrderClient orderClient = new OrderClient();

    @Given("add new order request is prepared")
    public void addNewOrderRequestIsPrepared() {
        OrderRequest orderRequest = OrderDataBuilder.prepareOrderRequest();
        traceId();
        TestDataSerenity.set(ORDER_REQUEST, orderRequest);
    }

    @When("order request is sent and gets {int}")
    public void orderRequestIsSentAndGets(int statusCode) {
        OrderRequest orderRequest = TestDataSerenity.get(ORDER_REQUEST, OrderRequest.class);
        String traceId = TestDataSerenity.get(TRACE_ID, String.class);
        CreateOrderResponse response = orderClient.addOrderRequest(orderRequest, traceId, statusCode);
        TestDataSerenity.set(CREATE_ORDER_RESPONSE, response);
    }
}