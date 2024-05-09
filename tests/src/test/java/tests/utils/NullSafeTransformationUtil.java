package tests.utils;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NullSafeTransformationUtil {

    public static  <T> T nullSafe(Supplier<T> source) {
        try {
            return source.get();
        } catch (NullPointerException e) {
            return null;
        } catch (NumberFormatException e) {
            if (e.getMessage().contains("null")) {
                return null;
            }
            throw e;
        }
    }
}
