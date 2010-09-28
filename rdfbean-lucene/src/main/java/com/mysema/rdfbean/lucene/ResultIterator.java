/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.compass.core.CompassHits;
import org.compass.core.Resource;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;

/**
 * ResultIterator provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class ResultIterator implements CloseableIterator<STMT>{

    private final CompassHits hits;
    
    private int index = -1;
    
    private Iterator<STMT> results;
    
    public ResultIterator(CompassHits hits){
        this.hits = Assert.notNull(hits,"hits");
    }

    @Override
    public void close() {
        hits.close();        
    }
    
    private void getNextResults() {
        if (results == null || !results.hasNext()){
            if (++index < hits.length()){
                results = getStatements(hits.resource(index)).iterator(); 
            }
        }
    }

    protected abstract List<STMT> getStatements(Resource resource);

    @Override
    public boolean hasNext() {
        getNextResults();
        return results.hasNext();
    }

    @Override
    public STMT next() {
        getNextResults();
        if (results.hasNext()){
            return results.next();    
        }else{
            throw new NoSuchElementException();
        }
        
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();        
    }
    
}
