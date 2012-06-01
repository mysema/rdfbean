/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.ontology.AbstractOntology;
import com.mysema.util.MultimapFactory;

/**
 * ConfigurationOntology provides a Configuration based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class ConfigurationOntology extends AbstractOntology{

    public ConfigurationOntology(Configuration configuration){
        Set<UID> types = new HashSet<UID>();
        Multimap<UID,UID> directSubtypes = MultimapFactory.<UID,UID>createWithSet();        
        Multimap<UID,UID> directSupertypes = MultimapFactory.<UID,UID>createWithSet();
        
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            types.add(mappedClass.getUID());
            for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                directSupertypes.put(mappedClass.getUID(), superClass.getUID());
                directSubtypes.put(superClass.getUID(), mappedClass.getUID());
            }
//            ClassMapping classMapping = mappedClass.getJavaClass().getAnnotation(ClassMapping.class);
//            if (!classMapping.parent().equals("")) {
//        	UID parent = new UID(classMapping.parent());
//        	directSupertypes.put(mappedClass.getUID(), parent);
//        	directSubtypes.put(parent, mappedClass.getUID());
//            }
        }        
        initializeTypeHierarchy(types, directSubtypes, directSupertypes);        
    }
    
}
