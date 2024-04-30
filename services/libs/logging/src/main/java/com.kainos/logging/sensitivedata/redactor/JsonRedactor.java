package com.kainos.logging.sensitivedata.redactor;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.zalando.logbook.BodyFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kainos.logging.sensitivedata.redactor.properties.GlobalRedactorProperties;
import com.kainos.logging.sensitivedata.redactor.properties.JsonRedactorProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonRedactor implements BodyFilter {
    private static final int ATTRIBUTE_OBJECT_FIELD_COUNT = 6;
    private static final String ATTRIBUTE_DATA_TYPE_FIELD_NAME = "datatype";
    private static final String ATTRIBUTE_VALUE_FIELD_NAME = "value";
    private static final List<String> ATTRIBUTE_KEY_FIELD_NAMES = List.of("name", "key");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonRedactorProperties jsonRedactorProperties;

    @Autowired
    private GlobalRedactorProperties globalRedactorProperties;

    private List<Pattern> redListPatterns;
    private List<Pattern> greenListPatterns;

    public String redact(String payload) {
        try {
            if (globalRedactorProperties.getEnabled()) {
                return Objects.toString(redact(() -> objectMapper.readTree(payload)), "");
            } else {
                return payload;
            }
        } catch (IOException | IllegalStateException e) {
            return payload;
        }
    }

    public String redact(InputStream payload) {
        try {
            if (globalRedactorProperties.getEnabled()) {
                return Objects.toString(redact(() -> objectMapper.readTree(payload)), null);
            } else {
                return convert(payload);
            }
        } catch (IOException | IllegalStateException e) {
            return convert(payload);
        }
    }

    @Override
    public String filter(@Nullable String contentType, String body) {
        if (isContentTypeJson(contentType)) {
            return redact(body);
        } else {
            return body;
        }
    }

    private boolean isContentTypeJson(String contentType) {
        return contentType != null && contentType.startsWith(APPLICATION_JSON_VALUE);
    }

    private static String convert(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private Object redact(SupplierWithIO<JsonNode> supplier) throws IOException {

        var node = supplier.getWithIO();
        if (!jsonRedactorProperties.getEnabled()) {
            return node;
        }
        if (checkInvalidConfig()) {
            log.warn("Invalid config, returning unredacted");
            return node;
        }

        if (node == null || node == MissingNode.getInstance()) {
            return "";
        }

        recurse("", "", node, null);
        return node;
    }

    private void recurse(String path, String fieldName, JsonNode node, JsonNode parentNode) {
        if (node.isObject()) {
            var objectNode = (ObjectNode) node;
            isAttributeLikeNode(objectNode).ifPresentOrElse(
                keyFieldName -> redactAttributeObject(keyFieldName, objectNode),
                () -> objectNode.fields().forEachRemaining(entry -> recurse(
                    path + "." + entry.getKey(),
                    entry.getKey(),
                    entry.getValue(),
                    objectNode))
            );
            return;
        }
        if (node.isArray()) {
            var arrayNode = ((ArrayNode) node);
            IntStream.range(0, arrayNode.size())
                .forEach(idx ->
                    recurse(
                        path + "[" + idx + "]",
                        fieldName,
                        arrayNode.get(idx),
                        node));
            redact(path, fieldName, parentNode);
            return;
        }
        //redact only number and text
        if (node.isTextual() || node.isNumber()) {
            redact(path, fieldName, parentNode);
        }
    }

    private void redact(String path, String fieldName, JsonNode parentNode) {
        var toRedact = redListPatterns.stream()
            .filter(pattern -> pattern.matcher(path).matches())
            .toList();
        var toAllow = greenListPatterns.stream()
            .filter(pattern -> pattern.matcher(path).matches())
            .toList();
        if (!toAllow.isEmpty()) {
            //if greenlisted, do not redact
            return;
        }
        if (!toRedact.isEmpty() && parentNode != null) {
            change(parentNode, fieldName);
        }
    }

    private void redactAttributeObject(String keyFieldName, ObjectNode node) {
        var hasDataTypeField = node.has(ATTRIBUTE_DATA_TYPE_FIELD_NAME);
        if (hasDataTypeField
            && jsonRedactorProperties.getAttributeDataTypeRedlist().contains(node.get(ATTRIBUTE_DATA_TYPE_FIELD_NAME).asText())) {
            redactValueField(node);
        } else {
            var keyName = node.get(keyFieldName).asText();
            redact(keyName, ATTRIBUTE_VALUE_FIELD_NAME, node);
        }
    }

    private void redactValueField(ObjectNode node) {
        change(node, ATTRIBUTE_VALUE_FIELD_NAME);
    }

    private Optional<String> isAttributeLikeNode(ObjectNode node) {

        for (var name : ATTRIBUTE_KEY_FIELD_NAMES) {
            if (!(node.has(name) && node.get(name).isTextual())) {
                continue;
            }
            var hasValueLikeField = node.has(ATTRIBUTE_VALUE_FIELD_NAME);

            var smallObject = List.of(node.fieldNames()).size() <= ATTRIBUTE_OBJECT_FIELD_COUNT;

            if (hasValueLikeField && smallObject) {
                return Optional.of(name);
            }
        }

        return Optional.empty();
    }

    private void change(JsonNode parent, String fieldName) {
        if (parent.has(fieldName)) {
            ((ObjectNode) parent).put(fieldName, parent.get(fieldName).isArray() ? jsonRedactorProperties.getRedactedArrayStamp()
                : jsonRedactorProperties.getRedactedStamp());
        }
    }

    private boolean checkInvalidConfig() {
        if (!CollectionUtils.isEmpty(redListPatterns)) {
            return false;
        }

        if (jsonRedactorProperties.getRedlist() == null) {
            log.warn("redlist = null, not redacting");
            return true;
        }
        if (jsonRedactorProperties.getRedlist().isEmpty()) {
            log.warn("redlist is empty, not redacting");
            return true;
        }

        redListPatterns = compilePatterns(jsonRedactorProperties.getRedlist());
        greenListPatterns = compilePatterns(jsonRedactorProperties.getGreenlist());

        return false;
    }

    private static List<Pattern> compilePatterns(List<String> list) {
        if (isEmpty(list)) {
            return List.of();
        }

        return list
            .stream()
            .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
            .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface SupplierWithIO<T> {
        T getWithIO() throws IOException;
    }
}
