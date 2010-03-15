/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import com.mysema.commons.lang.CloseableIterator;

/**
 * ResultIterator provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class ResultIterator implements CloseableIterator<STMT> {
    
    private final Iterator<STMT> iter;
    
//    private ResultIterator(Iterable<STMT> iterable, @Nullable final ID subject, @Nullable final UID predicate, 
//            @Nullable final NODE object, @Nullable final UID context, final boolean includeInferred) {
//        this(iterable.iterator(), subject, predicate, object, context, includeInferred);
//    }
    
    ResultIterator(Iterator<STMT> iterator, @Nullable final ID subject, @Nullable final UID predicate, 
            @Nullable final NODE object, @Nullable final UID context, final boolean includeInferred) {
        this.iter = new FilterIterator<STMT>(iterator, new Predicate<STMT>() {

            @Override
            public boolean evaluate(STMT stmt) {
                return STMTMatcher.matches(stmt, subject, predicate, object, context, includeInferred);
            }
            
        });
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public STMT next() {
        return iter.next();
    }

    @Override
    public void remove() {
    }
    
}