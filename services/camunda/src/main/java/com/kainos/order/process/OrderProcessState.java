package com.kainos.order.process;

import static java.lang.String.valueOf;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.kainos.order.error.CamundaErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderProcessState {
    public static final String TRACE_ID = "TRACE_ID";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String ORDER_NAME = "ORDER_NAME";

    public static String getTraceId(DelegateExecution delegateExecution) {
        return getCamundaNonNullVariable(delegateExecution, TRACE_ID);
    }

    public static Long getOrderId(DelegateExecution delegateExecution) {
        return getCamundaNonNullVariable(delegateExecution, ORDER_ID);
    }

    public static String getOrderName(DelegateExecution execution) {
        return getCamundaNonNullVariable(execution, ORDER_NAME);
    }

    private static <T> T getCamundaNonNullVariable(DelegateExecution delegateExecution, String name) {
        return (T) Optional.ofNullable(delegateExecution.getVariable(name))
            .orElseThrow(() -> new BpmnError(valueOf(CamundaErrorCode.MISSING_VARIABLE_ERROR),
                String.format("%s is missing", name)));
    }
}
