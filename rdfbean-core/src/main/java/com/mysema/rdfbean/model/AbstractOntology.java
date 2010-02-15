/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Set;
import java.util.Stack;

import com.mysema.util.SetMap;

/**
 * AbstractOntology provides a base implementation of the Ontology interface 
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractOntology implements Ontology{

    private final SetMap<UID, UID> subtypes = new SetMap<UID, UID>();
    
    private final SetMap<UID, UID> supertypes = new SetMap<UID, UID>();
    
    private final SetMap<UID, UID> subproperties = new SetMap<UID, UID>();
    
    private final SetMap<UID, UID> superproperties = new SetMap<UID, UID>();   

    protected void initializeTypeHierarchy(Set<UID> types, 
            SetMap<UID,UID> directSubtypes, 
            SetMap<UID, UID> directSupertypes){
        for (UID type : types){
            subtypes.put(type, type);            
            if (directSubtypes.containsKey(type)){
                flatten(type, directSubtypes, subtypes);
            }
            if (directSupertypes.containsKey(type)){
                flatten(type, directSupertypes, supertypes);
            }
        }
    }
    

    protected void initializePropertyHierarchy(Set<UID> properties, 
            SetMap<UID,UID> directSubproperties, 
            SetMap<UID, UID> directSuperproperties){
        for (UID property : properties){
            subproperties.put(property, property);            
            if (directSubproperties.containsKey(property)){
                flatten(property, directSubproperties, subproperties);
            }            
            if (directSuperproperties.containsKey(property)){
                flatten(property, directSuperproperties, superproperties);
            }
        }
    }
    
    private void flatten(UID id, SetMap<UID,UID> direct, SetMap<UID,UID> expanded){
        Stack<UID> t = new Stack<UID>();
        t.addAll(direct.get(id));
        while (!t.isEmpty()){
            UID supertype = t.pop();
            expanded.put(id, supertype);
            if (direct.containsKey(supertype)){
                t.addAll(direct.get(supertype));
            }
        }
    }
        
    @Override
    public Set<UID> getSubtypes(UID uid) {        
        return subtypes.get(uid);
    }

    @Override
    public Set<UID> getSupertypes(UID uid) {
        return supertypes.get(uid);
    }
    
    @Override
    public Set<UID> getSubproperties(UID uid) {        
        return subproperties.get(uid);
    }

    @Override
    public Set<UID> getSuperproperties(UID uid) {
        return superproperties.get(uid);
    }

}
