/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.ontology.AbstractOntology;
import com.mysema.util.MultiMapFactory;

/**
 * @author tiwe
 *
 */
public class RDBOntology extends AbstractOntology<Long>{

    private final IdFactory idFactory;
    
    public RDBOntology(IdFactory idFactory, Configuration configuration) {
        this.idFactory = idFactory;
        Set<Long> types = new HashSet<Long>();
        MultiMap<Long,Long> directSubtypes = MultiMapFactory.<Long,Long>createWithSet();        
        MultiMap<Long,Long> directSupertypes = MultiMapFactory.<Long,Long>createWithSet();
        
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            Long id = getId(mappedClass.getUID());
            types.add(id);
            for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                if (superClass.getUID() != null){
                    directSupertypes.put(id, getId(superClass.getUID()));
                    directSubtypes.put(getId(superClass.getUID()), id);    
                }                
            }
        }        
        initializeTypeHierarchy(types, directSubtypes, directSupertypes);               
    }
    
    private Long getId(UID uid){
        return idFactory.getId(uid);
    }
    
}
