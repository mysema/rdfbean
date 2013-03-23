/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinType;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * BeanListSourceBuilder provides an RDFBeanQuery style query builder for paged
 * results
 * 
 * @author tiwe
 * @version $Id$
 */
public class BeanListSourceBuilder {

    private final SessionFactory sessionFactory;

    private final List<EntityPath<?>> sources = new ArrayList<EntityPath<?>>();

    private final QueryMetadata metadata = new DefaultQueryMetadata();

    public BeanListSourceBuilder(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public BeanListSourceBuilder from(EntityPath<?>... from) {
        for (EntityPath<?> source : from) {
            sources.add(source);
            metadata.addJoin(JoinType.DEFAULT, source);
        }
        return this;
    }

    public BeanListSourceBuilder orderBy(OrderSpecifier<?>... o) {
        for (OrderSpecifier<?> order : o) {
            metadata.addOrderBy(order);
        }
        return this;
    }

    public BeanListSourceBuilder where(Predicate... o) {
        for (Predicate p : o) {
            metadata.addWhere(p);
        }
        return this;
    }

    public <T> BeanListSource<T> list(Path<T> projection) {
        EntityPath<?>[] sourceArray = sources.toArray(new EntityPath[sources.size()]);
        return new BeanListSource<T>(sessionFactory, sourceArray, metadata, projection);
    }

}
