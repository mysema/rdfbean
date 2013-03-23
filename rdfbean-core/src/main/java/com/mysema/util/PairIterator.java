package com.mysema.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

public class PairIterator<T> implements Iterator<T> {

    private final Iterator<T> firstIterator;

    @Nullable
    private Iterator<T> secondIterator;

    private final Iterable<T> second;

    @Nullable
    private Boolean hasNext;

    @Nullable
    private T value;

    public PairIterator(Iterable<T> first, Iterable<T> second) {
        this.firstIterator = first.iterator();
        this.second = second;
    }

    @Override
    public boolean hasNext() {
        while (hasNext == null) {
            produceNext();
        }
        return hasNext.booleanValue();
    }

    @Override
    public T next() {
        while (hasNext == null) {
            produceNext();
        }
        if (hasNext.booleanValue()) {
            hasNext = null;
            return value;
        } else {
            throw new NoSuchElementException();
        }
    }

    private void produceNext() {
        hasNext = true;

        // first
        if (secondIterator == null || !secondIterator.hasNext()) {
            secondIterator = null;
            if (!firstIterator.hasNext()) {
                hasNext = false;
                return;
            } else {
                firstIterator.next();
            }
        }

        // second
        if (secondIterator == null) {
            secondIterator = second.iterator();
            if (!secondIterator.hasNext()) {
                hasNext = null;
                return;
            }
        }

        value = secondIterator.next();

    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
