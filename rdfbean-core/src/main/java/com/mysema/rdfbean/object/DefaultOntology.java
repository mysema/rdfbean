/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.mysema.rdfbean.model.Ontology;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.SetMap;

/**
 * DefaultOntology provides a Configuration based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultOntology implements Ontology{

    private final SetMap<UID, UID> subtypes = new SetMap<UID, UID>();
    
    private final SetMap<UID, UID> supertypes = new SetMap<UID, UID>();    

    public DefaultOntology(Configuration configuration){
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

    @Override
    public Set<UID> getSubtypes(UID uid) {        
        return subtypes.get(uid);
    }

    @Override
    public Set<UID> getSupertypes(UID uid) {
        return supertypes.get(uid);
    }

    private void initializeTypeHierarchy(Set<UID> types, 
            SetMap<UID,UID> directSubtypes, SetMap<UID, UID> directSupertypes){
        for (UID type : types){
            subtypes.put(type, type);
            supertypes.put(type, type);
            
            // handle subtypes
            if (directSubtypes.containsKey(type)){
                Stack<UID> t = new Stack<UID>();
                t.addAll(directSubtypes.get(type));
                while (!t.isEmpty()){
                    UID subtype = t.pop();
                    subtypes.put(type, subtype);
                    if (directSubtypes.containsKey(subtype)){
                        t.addAll(directSubtypes.get(subtype));
                    }
                }
            }
            
            // handle supertypes
            if (directSupertypes.containsKey(type)){
                Stack<UID> t = new Stack<UID>();
                t.addAll(directSupertypes.get(type));
                while (!t.isEmpty()){
                    UID subtype = t.pop();
                    supertypes.put(type, subtype);
                    if (directSupertypes.containsKey(subtype)){
                        t.addAll(directSupertypes.get(subtype));
                    }
                }
            }
        }
    }
    
}
