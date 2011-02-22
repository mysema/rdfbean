package com.mysema.util;

import java.util.Iterator;

import com.mysema.query.QueryModifiers;
import com.mysema.query.util.LimitingIterator;

public class LimitingIterable<T> implements Iterable<T> {

    private final Iterable<T> iterable;

    private final QueryModifiers modifiers;

    public LimitingIterable(Iterable<T> iterable, QueryModifiers modifiers) {
        this.iterable = iterable;
        this.modifiers = modifiers;
    }

    @Override
    public Iterator<T> iterator() {
        return LimitingIterator.create(iterable.iterator(), modifiers);
    }

}
