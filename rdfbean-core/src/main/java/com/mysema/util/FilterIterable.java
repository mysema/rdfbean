package com.mysema.util;

import java.util.Iterator;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

public class FilterIterable<T> implements Iterable<T>{

    private final Iterable<T> iterable;
    
    private final Predicate<T> predicate;
    
    public FilterIterable(Iterable<T> iterable, Predicate<T> predicate) {
        this.iterable = iterable;
        this.predicate = predicate;        
    }
    
    @Override
    public Iterator<T> iterator() {
        return new FilterIterator<T>(iterable.iterator(), predicate);
    }
    
    

}
