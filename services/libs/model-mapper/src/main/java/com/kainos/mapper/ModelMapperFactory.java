package com.kainos.mapper;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.modelmapper.ModelMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelMapperFactory {

    public static ModelMapper getMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addConverter(
            context -> context.getSource() != null ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(context.getSource()) : null,
            OffsetDateTime.class, String.class);

        modelMapper.addConverter(
            context -> context.getSource() != null ? LocalDateTime.parse(context.getSource()) : null,
            String.class, LocalDateTime.class);

        modelMapper.addConverter(
            context -> context.getSource() != null ? LocalDate.parse(context.getSource()) : null,
            String.class, LocalDate.class);

        modelMapper.addConverter(
            context -> context.getSource() != null ? OffsetDateTime.parse(context.getSource()) : null,
            String.class, OffsetDateTime.class);

        modelMapper.addConverter(
            context -> context.getSource() != null ? URI.create(context.getSource()) : null,
            String.class, URI.class);

        return modelMapper;
    }
}
