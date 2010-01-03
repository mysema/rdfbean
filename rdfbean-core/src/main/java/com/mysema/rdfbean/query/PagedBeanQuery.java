/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryBase;
import com.mysema.query.QueryMixin;
import com.mysema.query.paging.ListSource;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.Path;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * PagedQuery provides an RDFBeanQuery style query builder for paged results
 *
 * @author tiwe
 * @version $Id$
 */
public class PagedBeanQuery extends QueryBase<PagedBeanQuery>{

    private final SessionFactory sessionFactory;
    
    private final List<PEntity<?>> sources = new ArrayList<PEntity<?>>();
    
    public PagedBeanQuery(SessionFactory sessionFactory){
        super(new QueryMixin<PagedBeanQuery>(new DefaultQueryMetadata()));
        this.queryMixin.setSelf(this);
        this.sessionFactory = sessionFactory;
    }
    
    public PagedBeanQuery from(PEntity<?>... from){
        for (PEntity<?> source : from){
            sources.add(source);    
        }        
        return this;
    }
    
    public <T> ListSource<T> list(final Path<T> projection){ 
        PEntity<?>[] sourceArray = sources.toArray(new PEntity[0]);        
        return new BeanListSource<T>(sessionFactory, sourceArray, queryMixin.getMetadata(), projection);
    }

}
