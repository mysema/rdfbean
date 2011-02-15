package com.mysema.util;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.iterators.IteratorChain;

public class IterableChain<T> implements Iterable<T>{
    
    private final List<Iterable<T>> iterables;
    
    public IterableChain(List<Iterable<T>> iterables) {
        this.iterables = iterables;
    }

    @Override
    public Iterator<T> iterator() {
        IteratorChain<T> chain = new IteratorChain<T>();
        for (Iterable<T> iterable : iterables){
            chain.addIterator(iterable.iterator());
        }
        return chain;
    }

}
