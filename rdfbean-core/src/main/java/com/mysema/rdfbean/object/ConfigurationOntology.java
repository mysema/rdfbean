/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.ontology.AbstractOntology;
import com.mysema.util.MultiMapFactory;

/**
 * ConfigurationOntology provides a Configuration based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class ConfigurationOntology extends AbstractOntology<UID>{

    public ConfigurationOntology(Configuration configuration){
        Set<UID> types = new HashSet<UID>();
        MultiMap<UID,UID> directSubtypes = MultiMapFactory.<UID,UID>createWithSet();        
        MultiMap<UID,UID> directSupertypes = MultiMapFactory.<UID,UID>createWithSet();
        
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            types.add(mappedClass.getUID());
            for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                directSupertypes.put(mappedClass.getUID(), superClass.getUID());
                directSubtypes.put(superClass.getUID(), mappedClass.getUID());
            }
        }        
        initializeTypeHierarchy(types, directSubtypes, directSupertypes);        
    }
    
}
