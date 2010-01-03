/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Collections;
import java.util.List;

import com.mysema.query.paging.ListSource;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionCallback;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * ListSourceBase provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class ListSourceBase<T> implements ListSource<T>{
    
    private final SessionFactory sessionFactory;
    
    private final long size;
    
    public ListSourceBase(SessionFactory sessionFactory, long size){
        this.sessionFactory = sessionFactory;
        this.size = size;
    }
    
    public static <T> ListSource<T> emptyResults() {
        return new ListSource<T>(){
            @Override
            public List<T> getResults(int fromIndex, int toIndex) {
                return Collections.emptyList();
            }
            @Override
            public boolean isEmpty() {
                return true;
            }
            @Override
            public long size() {
                return 0l;
            }                        
        };
    }
    
    protected abstract List<T> getInnerResults(Session session, int fromIndex, int toIndex);
    
    public List<T> getResults(final int fromIndex, final int toIndex){
        return sessionFactory.execute(new SessionCallback<List<T>>(){
            public List<T> doInSession(Session session){
                return getInnerResults(session, fromIndex, toIndex);
            }
        });
    }
    
    public final boolean isEmpty(){
        return size == 0l;
    }

    public final long size(){
        return size;
    }

}
