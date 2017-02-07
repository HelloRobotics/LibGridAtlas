package io.github.hellorobotics.lib.util;

import java.util.Optional;

/**
 * Author: Towdium
 * Date:   07/02/17
 */
public interface Wrapper<T> {
    Optional<T> get(int i);

    T[] toArray();
}
