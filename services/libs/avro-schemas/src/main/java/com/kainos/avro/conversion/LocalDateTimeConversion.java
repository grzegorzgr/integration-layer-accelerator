package com.kainos.avro.conversion;

import java.time.LocalDateTime;

import org.apache.avro.Conversion;
import org.apache.avro.Schema;

public class LocalDateTimeConversion extends Conversion<LocalDateTime> {
    @Override
    public Class<LocalDateTime> getConvertedType() {
        return LocalDateTime.class;
    }

    @Override
    public Schema getRecommendedSchema() {
        return Schema.create(Schema.Type.STRING);
    }

    @Override
    public String getLogicalTypeName() {
        return "java.time.LocalDateTime";
    }
}
