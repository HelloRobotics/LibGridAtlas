/*
 * Copyright 2017 HelloRobotics.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

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
