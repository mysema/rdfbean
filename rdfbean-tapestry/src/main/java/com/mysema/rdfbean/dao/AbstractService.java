/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.dao;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
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
public abstract class AbstractService {
    
    @Inject
    private SessionFactory sessionFactory;
    
    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }    
    
    protected BeanListSourceBuilder getPagedQuery(){
      return new BeanListSourceBuilder(sessionFactory);
    }
    
    protected <T> GridDataSource createGridDataSource(PEntity<T> entity, OrderSpecifier<?> defaultOrder){
        return new BeanGridDataSource<T>(sessionFactory, entity, defaultOrder, null);
    }
    
    protected <T> GridDataSource createGridDataSource(PEntity<T> entity, OrderSpecifier<?> defaultOrder, EBoolean conditions){
        return new BeanGridDataSource<T>(sessionFactory, entity, defaultOrder, conditions);
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
}
