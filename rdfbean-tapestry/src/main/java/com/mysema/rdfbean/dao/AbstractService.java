/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.dao;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.query.BeanListSourceBuilder;
import com.mysema.rdfbean.tapestry.BeanGridDataSource;

/**
 * AbstractService provides a stub for transactional RDFBean DAOs/Repositories
 *
 * @author tiwe
 * @version $Id$
 */
@Deprecated
public abstract class AbstractService {
    
    private final SessionFactory sessionFactory;
    
    public AbstractService(@Inject SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }
    
    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }    
    
    protected BeanListSourceBuilder getPagedQuery(){
      return new BeanListSourceBuilder(sessionFactory);
    }
    
    protected <T> GridDataSource createGridDataSource(EntityPath<T> entity, OrderSpecifier<?> defaultOrder, boolean caseSensitive){
        return new BeanGridDataSource<T>(sessionFactory, entity, defaultOrder, caseSensitive, null);
    }
    
    protected <T> GridDataSource createGridDataSource(EntityPath<T> entity, OrderSpecifier<?> defaultOrder, boolean caseSensitive, Predicate conditions){
        return new BeanGridDataSource<T>(sessionFactory, entity, defaultOrder, caseSensitive, conditions);
    }

}
