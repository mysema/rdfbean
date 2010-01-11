/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.dao;

import java.util.Collection;

import com.mysema.query.types.path.PEntity;

/**
 * AbstractRepository provides a basic stub for Repository implementations
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRepository<T> extends AbstractService 
    implements Repository<T,String>{
    
    private final PEntity<T> entity;
    
    protected AbstractRepository(PEntity<T> entity){
        this.entity = entity;
    }
    
    @SuppressWarnings("unchecked")
    protected Class<T> getType(){
        return (Class<T>) entity.getType();
    }
    
    @Override
    public Collection<T> getAll() {
        return getSession().findInstances(getType());
    }

    @Override
    public T getById(String id) {
        return getSession().getById(id, getType());
    }

    @Override
    public void remove(T entity) {
        getSession().delete(entity);        
    }

    @Override
    public T save(T entity) {
        getSession().save(entity);
        return entity;
    }

    @Override
    public void saveAll(Iterable<? extends T> entities) {
        for (T t : entities) {
            getSession().save(t);
        }
    }

    @Override
    public void remove(String id) {
        T entity = getById(id);
        if (entity != null){
            getSession().delete(entity);
        }
    }
    
}
