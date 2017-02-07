/*
 *  Copyright 2016 HelloRobotics.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.github.hellorobotics.lib.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Towdium
 * Date:   02/02/17
 */
public interface Section<E> extends Collection<E> {
    E remove(boolean forward);

    boolean addAll(Collection<? extends E> c, boolean forward);

    boolean add(E e, boolean forward);

    Optional<E> get(int index);

    Optional<E> set(int index, E e);

    int start();

    int end();

    int indexOf(Object o);

    int lastIndexOf(Object o);

    Map<Integer, E> toMap();

    SectionIterator<E> sectionIterator();

    SectionIterator<E> sectionIterator(int index);
}
