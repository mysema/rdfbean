/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;

import com.mysema.commons.lang.Assert;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PComparable;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionCallback;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * RDFBeanDataSource provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class BeanGridDataSource<T> implements GridDataSource {

    private final SessionFactory sessionFactory;

    private final Class<T> entityType;
    
    private final PathBuilder<T> entityPath;

    private int startIndex;

    private List<T> preparedResults;
    
    @Nullable
    private final EBoolean conditions;

    public BeanGridDataSource(SessionFactory sessionFactory, PEntity<T> entity) {
        this(sessionFactory, entity, null);
    }
    
    @SuppressWarnings("unchecked")
    public BeanGridDataSource(SessionFactory sessionFactory, PEntity<T> entity, @Nullable EBoolean conditions) {
        this.sessionFactory = Assert.notNull(sessionFactory);
        this.entityType = (Class<T>) Assert.notNull(entity.getType());
        this.entityPath = new PathBuilder<T>(entity.getType(), entity.getMetadata());
        this.conditions = conditions;
    }
    
    public int getAvailableRows() {
        return sessionFactory.execute(new SessionCallback<Integer>(){
            @Override
            public Integer doInSession(Session session) {
                BeanQuery beanQuery = session.from(entityPath);
                if (conditions != null){
                    beanQuery.where(conditions);
                }
                return (int) beanQuery.count();
            }            
        });
    }

    public void prepare(final int start, final int end, final List<SortConstraint> sortConstraints) {
        Assert.notNull(sortConstraints);   
        sessionFactory.execute(new SessionCallback<Void>(){
            @Override
            public Void doInSession(Session session) {
                prepare(session, start, end, sortConstraints);
                return null;
            }
            
        });
    }

    @SuppressWarnings("unchecked")
    private void prepare(Session session, int startIndex, int endIndex, List<SortConstraint> sortConstraints) {
        BeanQuery beanQuery = session.from(entityPath);
        beanQuery.offset(startIndex);
        beanQuery.limit(endIndex - startIndex + 1);        
        for (SortConstraint constraint : sortConstraints) {
            String propertyName = constraint.getPropertyModel().getPropertyName();
            Class<? extends Comparable<?>> propertyType = constraint.getPropertyModel().getPropertyType();
            PComparable<?> propertyPath = entityPath.getComparable(propertyName, propertyType);
            switch (constraint.getColumnSort()) {
                case ASCENDING:  beanQuery.orderBy(propertyPath.asc()); break; 
                case DESCENDING: beanQuery.orderBy(propertyPath.desc()); break;
            }
        }
        if (conditions != null){
            beanQuery.where(conditions);
        }
        this.startIndex = startIndex;
        preparedResults = beanQuery.list(entityPath);
    }
    
    public Object getRowValue(int index) {
        return preparedResults.get(index - startIndex);
    }

    public Class<?> getRowType() {
        return entityType;
    }

}
