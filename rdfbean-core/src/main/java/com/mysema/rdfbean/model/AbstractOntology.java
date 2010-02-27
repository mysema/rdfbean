/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.MultiMap;

import com.mysema.util.MultiMapFactory;

/**
 * AbstractOntology provides a base implementation of the Ontology interface 
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractOntology implements Ontology{

    private final MultiMap<UID, UID> subtypes = MultiMapFactory.<UID, UID>createWithSet();
    
    private final MultiMap<UID, UID> supertypes = MultiMapFactory.<UID, UID>createWithSet();
    
    private final MultiMap<UID, UID> subproperties = MultiMapFactory.<UID, UID>createWithSet();
    
    private final MultiMap<UID, UID> superproperties = MultiMapFactory.<UID, UID>createWithSet();   

    protected void initializeTypeHierarchy(Set<UID> types, 
            MultiMap<UID,UID> directSubtypes, 
            MultiMap<UID, UID> directSupertypes){
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
            MultiMap<UID,UID> directSubproperties, 
            MultiMap<UID, UID> directSuperproperties){
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
    
    private void flatten(UID id, MultiMap<UID,UID> direct, MultiMap<UID,UID> expanded){
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
    public Collection<UID> getSubtypes(UID uid) {        
        Collection<UID> rv = subtypes.get(uid);
        return rv != null ? rv : Collections.<UID>emptySet();
    }

    @Override
    public Collection<UID> getSupertypes(UID uid) {
        Collection<UID> rv =  supertypes.get(uid);
        return rv != null ? rv : Collections.<UID>emptySet();
    }
    
    @Override
    public Collection<UID> getSubproperties(UID uid) {        
        Collection<UID> rv =  subproperties.get(uid);
        return rv != null ? rv : Collections.<UID>emptySet();
    }

    @Override
    public Collection<UID> getSuperproperties(UID uid) {
        Collection<UID> rv =  superproperties.get(uid);
        return rv != null ? rv : Collections.<UID>emptySet();
    }

}
