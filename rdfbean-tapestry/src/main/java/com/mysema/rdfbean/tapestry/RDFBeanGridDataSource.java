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

/**
 * RDFBeanDataSource provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class RDFBeanGridDataSource<T> implements GridDataSource {

    private final Session session;

    private final Class<T> entityType;
    
    private final PathBuilder<T> entityPath;

    private int startIndex;

    private List<T> preparedResults;

    public RDFBeanGridDataSource(Session session, Class<T> entityType) {
        this.session = Assert.notNull(session, "session");
        this.entityType = Assert.notNull(entityType, "entityType");
        this.entityPath = new PathBuilder<T>(entityType, "entity");
    }

    public int getAvailableRows() {
        // TODO : tx
        BeanQuery beanQuery = getSession().from(entityPath);
        applyConstraints(beanQuery);
        return (int) beanQuery.count();
    }

    public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints) {
        Assert.notNull(sortConstraints);
        // TODO : tx
        BeanQuery beanQuery = getSession().from(entityPath);
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

    protected Session getSession(){
        return session;
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
