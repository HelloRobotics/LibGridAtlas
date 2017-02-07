package io.github.hellorobotics.lib.util;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date:   07/02/17
 */
public class Single<T> implements Wrapper<T> {
    private T value;
    private Predicate<T> predicate = t -> true;

    public Single() {}

    public Single(T value) {
        this.value = value;
    }

    public Single<T> push(T value) {
        if(value != null && predicate.test(value))
            this.value = value;
        return this;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Single<T> push(Optional<T> value) {
        if (value.isPresent()) {
            this.value = value.get();
        }
        return this;
    }

    public Optional<T> getValue() {
        return Optional.of(value);
    }

    @Override
    public Optional<T> get(int i) {
        if (i == 1)
            return Optional.of(value);
        else
            return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) new Object[]{value};
    }
}
