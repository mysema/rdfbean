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
 * Repository defines a Repository/DAO interface for transactional RDFBean repositories
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface Repository<Entity, Id extends Serializable> {    

    /**
     * Get all persisted instances
     * 
     * @return
     */
    Collection<Entity> getAll();

    /**
     * Get the persisted instance with the given id
     * 
     * @param id
     * @return
     */
    @Nullable
    Entity getById( Id id );

    /**
     * Remove the persisted instance
     * 
     * @param entity
     */
    void remove( Entity entity );
    
    /**
     * Removed the persisted entity with the given id
     * 
     * @param id
     */
    void remove(Id id);

    /**
     * Save the given entity
     * 
     * @param entity
     * @return
     */
    Entity save( Entity entity );
    
    /**
     * Save all the given entities
     * 
     * @param entities
     */
    void saveAll( Iterable<? extends Entity> entities);

}
