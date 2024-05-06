package tests.utils;

import static tests.model.TestDataKeys.TRACE_ID;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.core.Serenity;
import tests.model.TestDataKeys;

@Slf4j
public final class TestDataSerenity {
    private TestDataSerenity() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String traceId() {

        if (TestDataSerenity.get(TRACE_ID, String.class) == null) {
            String traceId = UUID.randomUUID().toString();
            TestDataSerenity.set(TRACE_ID, traceId);
        }

        log.info("TraceId helper method. TestDataSernity value is {}", TestDataSerenity.get(TRACE_ID, String.class));

        return TestDataSerenity.get(TRACE_ID, String.class);
    }

    public static void set(TestDataKeys key, Response value) {
        Serenity.setSessionVariable(key).to(value);
        Serenity.recordReportData().withTitle(key.toString())
            .andContents(value.asString());
    }

    public static void set(TestDataKeys key, Object value) {
        Serenity.setSessionVariable(key).to(value);
        try {
            Serenity.recordReportData().withTitle(key.toString())
                .andContents(ObjectMapperBuilder.build().writeValueAsString(value));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void set(String key, Object value) {
        Serenity.setSessionVariable(key).to(value);
    }

    public static <T> T get(TestDataKeys key, Class<T> clazz) {
        return clazz.cast(Serenity.sessionVariableCalled(key));
    }

    public static <T> T get(String key, Class<T> clazz) {
        return clazz.cast(Serenity.sessionVariableCalled(key));
    }

}
