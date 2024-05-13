package com.kainos.order.process;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.Random;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.order.process.models.ProcessName;
import com.kainos.orders.api.model.CreateOrderResponse;
import com.kainos.orders.api.model.Order;
import com.kainos.orders.api.model.OrderRequest;
import com.kainos.tracing.TraceContextProvider;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Autowired
    private OrderService orderService;

    @PostMapping
    @ResponseBody
    @ResponseStatus(CREATED)
    public CreateOrderResponse order(@RequestBody @Valid OrderRequest orderRequest) {
        String traceId = traceContextProvider.traceId();

        Order order = new Order()
            .id(generateOrderId())
            .name(orderRequest.getName());

        ProcessInstanceWithVariables processInstance = orderService.startCamundaProcess(
            ProcessName.ORDER,
            traceId,
            order);

        log.info("New order process started with instance id: {}", processInstance.getProcessInstanceId());

        return new CreateOrderResponse().id(order.getId());
    }

    private Long generateOrderId() {
        return new Random().nextLong(1L, 1000L);
    }
}
