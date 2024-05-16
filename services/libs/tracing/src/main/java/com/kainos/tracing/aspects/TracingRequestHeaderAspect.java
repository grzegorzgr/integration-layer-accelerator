package com.kainos.tracing.aspects;

import static com.kainos.tracing.TracingConfiguration.STANDARD_BAGGAGES_LIST;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class TracingRequestHeaderAspect {

    @Autowired
    private Tracer tracer;

    @PostConstruct
    public void logInfoAboutLoadedAspect() {
        log.info("Loaded TracingRequestHeaderAspect aspect on RestTemplate");
    }

    @Pointcut("execution(* org.springframework.web.client.RestTemplate.*(..))")
    public void springRestTemplatePointcut() { }

    @Around("springRestTemplatePointcut()")
    public Object addTraceIdToRequestFromContext(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] signatureArgs = proceedingJoinPoint.getArgs();
        List newArgs = new LinkedList<>();
        for (Object signatureArg: signatureArgs) {
            if (signatureArg instanceof HttpEntity) {
                HttpEntity originalHttpEntity = (HttpEntity) signatureArg;
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                headers.addAll(originalHttpEntity.getHeaders());
                STANDARD_BAGGAGES_LIST.forEach(
                    baggage -> {
                        if (!headers.containsKey(baggage) && StringUtils.isNotBlank(tracer.getAllBaggage().get(baggage))) {
                            headers.add(baggage, tracer.getAllBaggage().get(baggage));
                        }
                    }
                );
                newArgs.add(new HttpEntity<>(originalHttpEntity.getBody(), headers));
                continue;
            }
            newArgs.add(signatureArg);
        }
        return proceedingJoinPoint.proceed(newArgs.toArray());
    }
}
