/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import java.util.List;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;

import com.mysema.commons.lang.Assert;
import com.mysema.query.types.path.PComparable;
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
public class RDFBeanGridDataSource<T> implements GridDataSource {

    private final SessionFactory sessionFactory;

    private final Class<T> entityType;
    
    private final PathBuilder<T> entityPath;

    private int startIndex;

    private List<T> preparedResults;

    public RDFBeanGridDataSource(SessionFactory sessionFactory, Class<T> entityType) {
        this.sessionFactory = Assert.notNull(sessionFactory);
        this.entityType = Assert.notNull(entityType);
        this.entityPath = new PathBuilder<T>(entityType, "entity");
    }
    
    public int getAvailableRows() {
        return sessionFactory.execute(new SessionCallback<Integer>(){
            @Override
            public Integer doInSession(Session session) {
                BeanQuery beanQuery = session.from(entityPath);
                applyConstraints(beanQuery);
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

    private void prepare(Session session, int startIndex, int endIndex, List<SortConstraint> sortConstraints) {
        BeanQuery beanQuery = session.from(entityPath);
        beanQuery.offset(startIndex);
        beanQuery.limit(endIndex - startIndex);
        
        for (SortConstraint constraint : sortConstraints) {
            String propertyName = constraint.getPropertyModel().getPropertyName();
            PComparable<?> propertyPath = entityPath.getComparable(propertyName, Comparable.class);
            switch (constraint.getColumnSort()) {
                case ASCENDING:  beanQuery.orderBy(propertyPath.asc()); break; 
                case DESCENDING: beanQuery.orderBy(propertyPath.desc()); break;
            }
        }

        applyConstraints(beanQuery);
        this.startIndex = startIndex;
        preparedResults = beanQuery.list(entityPath);
    }
    
    protected void applyConstraints(BeanQuery beanQuery) {
        
    }
    
    protected PathBuilder<T> getEntityPath(){
        return entityPath;
    }

    public Object getRowValue(int index) {
        return preparedResults.get(index - startIndex);
    }

    public Class<?> getRowType() {
        return entityType;
    }

}
