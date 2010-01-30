/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.HashSet;
import java.util.Set;

import com.mysema.rdfbean.model.AbstractOntology;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.SetMap;

/**
 * ConfigurationOntology provides a Configuration based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class ConfigurationOntology extends AbstractOntology{

    public ConfigurationOntology(Configuration configuration){
        Set<UID> types = new HashSet<UID>();
        SetMap<UID,UID> directSubtypes = new SetMap<UID,UID>();        
        SetMap<UID,UID> directSupertypes = new SetMap<UID,UID>();
        
        for (Class<?> clazz : configuration.getMappedClasses()){
            MappedClass mappedClass = MappedClass.getMappedClass(clazz);
            types.add(mappedClass.getUID());
            for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                directSupertypes.put(mappedClass.getUID(), superClass.getUID());
                directSubtypes.put(superClass.getUID(), mappedClass.getUID());
            }
        }        
        initializeTypeHierarchy(types, directSubtypes, directSupertypes);        
    }



    
}
