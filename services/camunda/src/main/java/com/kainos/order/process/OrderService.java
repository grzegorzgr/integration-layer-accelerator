package com.kainos.order.process;

import static com.kainos.order.process.OrderProcessState.ORDER_ID;
import static com.kainos.order.process.OrderProcessState.ORDER_NAME;
import static com.kainos.order.process.OrderProcessState.TRACE_ID;

import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kainos.order.process.models.ProcessName;
import com.kainos.orders.api.model.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private RuntimeService runtimeService;

    public ProcessInstanceWithVariables startCamundaProcess(ProcessName processName, String traceId, Order order) {
        String businessKey = String.format("%s-%s", processName, traceId);

        log.info("Starting Camunda process: {} with business key: {}", processName, businessKey);

        return runtimeService
            .createProcessInstanceByKey(processName.name())
            .businessKey(businessKey)
            .setVariables(getProcessVariables(order, traceId))
            .executeWithVariablesInReturn();
    }

    private static Map<String, Object> getProcessVariables(Order order, String traceId) {
        return Map.of(
            TRACE_ID, traceId,
            ORDER_ID, order.getId(),
            ORDER_NAME, order.getName());
    }
}
