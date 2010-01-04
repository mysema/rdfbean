/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.paging.ListSource;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.Path;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * BeanListSourceBuilder provides an RDFBeanQuery style query builder for paged results
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanListSourceBuilder{

    private final SessionFactory sessionFactory;
    
    private final List<PEntity<?>> sources = new ArrayList<PEntity<?>>();
    
    private final QueryMetadata metadata = new DefaultQueryMetadata();
    
    public BeanListSourceBuilder(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }
    
    public BeanListSourceBuilder from(PEntity<?>... from){
        for (PEntity<?> source : from){
            sources.add(source);    
        }        
        return this;
    }
    
    public BeanListSourceBuilder orderBy(OrderSpecifier<?>... o){
        for (OrderSpecifier<?> order : o){
            metadata.addOrderBy(order);    
        }
        return this;
    }
    
    public BeanListSourceBuilder where(EBoolean... o){
        metadata.addWhere(o);
        return this;
    }
    
    public <T> ListSource<T> list(Path<T> projection){ 
        PEntity<?>[] sourceArray = sources.toArray(new PEntity[0]);        
        return new BeanListSource<T>(sessionFactory, sourceArray, metadata, projection);
    }

}
