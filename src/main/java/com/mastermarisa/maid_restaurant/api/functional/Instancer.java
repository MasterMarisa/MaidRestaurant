package com.mastermarisa.maid_restaurant.api.functional;

@FunctionalInterface
public interface Instancer<T> {
    T instance();
}
