package com.mastermarisa.maid_restaurant.uitls;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MergeUtil {
    @Nullable
    @SafeVarargs
    public static <T> T mergeNullable(Supplier<@Nullable T>... suppliers) {
        for (var supplier : suppliers) {
            T value = supplier.get();
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
