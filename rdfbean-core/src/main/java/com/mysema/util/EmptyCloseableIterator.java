package com.mysema.util;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.mysema.commons.lang.CloseableIterator;

/**
 * EmptyCloseableIterator provides
 *
 * @author tiwe
 * @version $Id$
 */
public class EmptyCloseableIterator<T> implements CloseableIterator<T> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        
    }

}
