package com.kainos.order.process.delegates;

import static com.kainos.order.process.OrderProcessState.getOrderId;
import static com.kainos.order.process.OrderProcessState.getOrderName;
import static com.kainos.order.process.OrderProcessState.getTraceId;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kainos.kafka.KafkaProducer;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateOrderDelegate implements JavaDelegate {

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Autowired
    private KafkaProducer kafkaProducer;


    @Override
    public void execute(DelegateExecution delegateExecution) {
        traceContextProvider.newSpanWithTraceId(getTraceId(delegateExecution), CreateOrderDelegate.class.getName(), () -> {
            Long orderId = getOrderId(delegateExecution);
            String orderName = getOrderName(delegateExecution);
            log.info("CreateOrderDelegate: execute for order - id: {}, name: {}", orderId, orderName);

            com.kainos.orders.avro.Order orderAvro = com.kainos.orders.avro.Order.newBuilder()
                .setId(orderId)
                .setName(orderName)
                .build();

            kafkaProducer.send(ordersTopic, orderId.toString(), orderAvro);
        });
    }
}
