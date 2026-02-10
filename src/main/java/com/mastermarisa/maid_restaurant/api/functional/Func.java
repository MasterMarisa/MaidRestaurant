package com.mastermarisa.maid_restaurant.api.functional;

@FunctionalInterface
public interface Func<T,F> {
    F accept(T t);
}
