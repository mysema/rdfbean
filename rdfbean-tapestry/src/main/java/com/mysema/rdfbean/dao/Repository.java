/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.dao;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Repository provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface Repository<Entity, Id extends Serializable> {    

    /**
     * @return
     */
    Collection<Entity> getAll();

    /**
     * @param id
     * @return
     */
    @Nullable
    Entity getById( Id id );

    /**
     * @param entity
     */
    void remove( Entity entity );
    
    /**
     * @param id
     */
    void remove(Id id);

    /**
     * @param entity
     * @return
     */
    Entity save( Entity entity );
    
    /**
     * @param entities
     */
    void saveAll( Iterable<? extends Entity> entities);

}
