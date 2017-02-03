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

import java.io.Serializable;
import java.util.*;

/**
 * Author: Towdium
 * Date:   02/02/17
 */
@SuppressWarnings("Duplicates")
public class ArraySection<E> implements Section<E>, Cloneable, Serializable {
    private static final Object[] EMPTY_ELEMENT = {};
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private transient Object[] elementData;
    private transient int start;
    private transient int size;
    private transient int offset;
    private transient int modCount;

    public ArraySection() {
        this(0, 0);
    }

    public ArraySection(int initialCapacity) {
        this(0, initialCapacity);
    }

    public ArraySection(int initialIndex, int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.elementData = initialCapacity == 0 ? EMPTY_ELEMENT : new Object[initialCapacity];
        start = initialCapacity <= 1 ? 0 : (initialCapacity / 2 - 1);
        size = 0;
        offset = initialIndex - start;
    }

    public ArraySection(Collection<? extends E> c) {
        this(0, c);
    }

    public ArraySection(int initialIndex, Collection<? extends E> c) {
        elementData = c.toArray();
        start = 0;
        size = elementData.length;
        offset = initialIndex;
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    }

    @Override
    public E remove(boolean forward) {
        if (size == 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg());
        modCount++;
        E ret;
        if (forward) {
            int i = start + size - 1;
            ret = elementData(i);
            elementData[i] = null;
        } else {
            ret = elementData(start);
            elementData[start] = null;
            start++;
        }
        size--;
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends E> c, boolean forward) {
        Object[] a = c.toArray();
        int numNew = a.length;
        modCount++;
        ensureAvailableCapacity(numNew, true);
        if (forward) {
            System.arraycopy(a, 0, elementData, size + start, numNew);
        } else {
            reverseArray(a);
            System.arraycopy(a, 0, elementData, start - numNew, numNew);
            start -= numNew;
        }
        size += numNew;
        return numNew != 0;
    }

    @Override
    public boolean add(E e, boolean forward) {
        modCount++;
        ensureAvailableCapacity(1, forward);
        elementData[forward ? start + size : start - 1] = e;
        if (!forward)
            start--;
        size++;
        return true;
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        return elementData(index - offset);
    }

    @Override
    public E set(int index, E e) {
        rangeCheck(index);
        modCount++;
        E oldValue = elementData(index - offset);
        elementData[index - offset] = e;
        return oldValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != Integer.MAX_VALUE;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = start; i < start + size; i++)
                if (elementData[i] == null)
                    return i + offset;
        } else {
            for (int i = start; i < start + size; i++)
                if (o.equals(elementData[i]))
                    return i + offset;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = start + size - 1; i >= start; i--)
                if (elementData[i] == null)
                    return i + offset;
        } else {
            for (int i = start + size - 1; i >= start; i--)
                if (o.equals(elementData[i]))
                    return i + offset;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Map<Integer, E> toMap() {
        HashMap<Integer, E> ret = new HashMap<>(size);
        SectionIterator<E> i = sectionIterator();
        while (i.hasNext()) {
            ret.put(i.nextIndex(), i.next());
        }
        return ret;
    }

    @Override
    public SectionIterator<E> sectionIterator() {
        return new ArraySectionIterator();
    }

    @Override
    public SectionIterator<E> sectionIterator(int index) {
        return new ArraySectionIterator(index);
    }

    @Override
    public Iterator<E> iterator() {
        return new ArraySectionIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] ret = new Object[size];
        System.arraycopy(elementData, start, ret, 0, size);
        return ret;
    }

    @Override
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) toArray();

        System.arraycopy(elementData, start, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        return add(e, true);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(c, true);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        modCount++;
        for (int i = start; i < start + size; i++)
            elementData[i] = null;
        size = 0;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (E e : this)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Section))
            return false;

        SectionIterator<E> e1 = sectionIterator();
        SectionIterator<?> e2 = ((Section<?>) o).sectionIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            ArraySection<?> v = (ArraySection<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, elementData.length);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private String outOfBoundsMsg() {
        return "The container is empty";
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Range: [" + (offset + start) + ", " + (offset + start + size) + "].";
    }

    private <T> T[] reverseArray(T[] arr) {
        T temp;
        int r = arr.length;
        for (int i = 0; i < arr.length / 2; i++) {
            temp = arr[i];
            arr[i] = arr[--r];
            arr[r] = temp;
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    private E elementData(int pos) {
        return (E) elementData[pos];
    }

    private void ensureAvailableCapacity(int minCapacity, boolean forward) {
        if (forward) {
            if (elementData.length - start - size < minCapacity)
                grow(minCapacity);
        } else {
            if (start < minCapacity)
                grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        modCount++;
        int dest = elementData.length;
        if (dest == 0)
            dest = 8;
        while ((dest - size) / 2 < minCapacity) {
            dest *= 2;
        }
        Object[] buf;
        int newStart;
        if (dest > MAX_ARRAY_SIZE) {
            if (elementData.length == MAX_ARRAY_SIZE || MAX_ARRAY_SIZE - size < minCapacity) {
                throw new OutOfMemoryError();
            } else {
                buf = new Object[MAX_ARRAY_SIZE];
                newStart = (MAX_ARRAY_SIZE - size) / 2;
            }
        } else {
            buf = new Object[dest];
            newStart = (dest - size) / 2;
        }
        System.arraycopy(elementData, start, buf, newStart, size);
        offset = offset + start - newStart;
        start = newStart;
        elementData = buf;
    }

    private void rangeCheck(int index) {
        if (index >= offset + start + size || index < offset + start)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private class ArraySectionIterator implements SectionIterator<E> {
        int cursor;
        int lastRet;
        int expectedModCount = modCount;

        public ArraySectionIterator() {
            this(offset + start);
        }

        public ArraySectionIterator(int index) {
            rangeCheck(index);
            cursor = index;
            lastRet = Integer.MAX_VALUE;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != offset + start;
        }

        @Override
        public E previous() {
            checkForComodification();
            try {
                int i = cursor - 1;
                E previous = get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(E e) {
            if (lastRet == Integer.MAX_VALUE)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArraySection.this.set(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            return cursor != offset + start + size;
        }

        @Override
        public E next() {
            checkForComodification();
            try {
                int i = cursor;
                E next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
}
