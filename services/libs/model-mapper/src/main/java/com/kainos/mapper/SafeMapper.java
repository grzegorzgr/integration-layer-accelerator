package com.kainos.mapper;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// Do not use method reference as input Supplier!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SafeMapper {
    private static final String NULL = "null";

    public static <T> T nullSafe(Supplier<T> source) {
        try {
            return source.get();
        } catch (NullPointerException e) {
            return null;
        } catch (NumberFormatException e) {
            if (e.getMessage().contains(NULL)) {
                return null;
            }
            throw e;
        }
    }

    public static <T> T nullSafeArray(Supplier<T> source) {
        try {
            return source.get();
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return null;
        } catch (NumberFormatException e) {
            if (e.getMessage().contains(NULL)) {
                return null;
            }
            throw e;
        }
    }

    public static <T> T nullSafeOptional(Supplier<T> source) {
        try {
            return source.get();
        } catch (NullPointerException | NoSuchElementException e) {
            return null;
        } catch (NumberFormatException e) {
            if (e.getMessage().contains(NULL)) {
                return null;
            }
            throw e;
        }
    }

    public static <T> List<T> listSafe(Supplier<List<T>> source) {
        return emptyIfNull(nullSafe(source));
    }

    public static <T> Stream<T> streamSafe(Supplier<List<T>> source) {
        return listSafe(source).stream();
    }
}
