/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.MultiMap;

import com.mysema.util.MultiMapFactory;

/**
 * AbstractOntology provides a generic implementation of the Ontology interface 
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractOntology<T>{

    private final MultiMap<T, T> subtypes = MultiMapFactory.<T, T>createWithSet();
    
    private final MultiMap<T, T> supertypes = MultiMapFactory.<T, T>createWithSet();
    
    private final MultiMap<T, T> subproperties = MultiMapFactory.<T, T>createWithSet();
    
    private final MultiMap<T, T> superproperties = MultiMapFactory.<T, T>createWithSet();   

    protected void initializeTypeHierarchy(Set<T> types, 
            MultiMap<T,T> directSubtypes, 
            MultiMap<T, T> directSupertypes){
        for (T type : types){
            subtypes.put(type, type);            
            if (directSubtypes.containsKey(type)){
                flatten(type, directSubtypes, subtypes);
            }
            if (directSupertypes.containsKey(type)){
                flatten(type, directSupertypes, supertypes);
            }
        }
    }
    

    protected void initializePropertyHierarchy(Set<T> properties, 
            MultiMap<T,T> directSubproperties, 
            MultiMap<T, T> directSuperproperties){
        for (T property : properties){
            subproperties.put(property, property);            
            if (directSubproperties.containsKey(property)){
                flatten(property, directSubproperties, subproperties);
            }            
            if (directSuperproperties.containsKey(property)){
                flatten(property, directSuperproperties, superproperties);
            }
        }
    }
    
    private void flatten(T id, MultiMap<T,T> direct, MultiMap<T,T> expanded){
        Stack<T> t = new Stack<T>();
        t.addAll(direct.get(id));
        while (!t.isEmpty()){
            T supertype = t.pop();
            expanded.put(id, supertype);
            if (direct.containsKey(supertype)){
                t.addAll(direct.get(supertype));
            }
        }
    }
        
    public Collection<T> getSubtypes(T uid) {        
        Collection<T> rv = subtypes.get(uid);
        return rv != null ? rv : Collections.singleton(uid);
    }

    public Collection<T> getSupertypes(T uid) {
        Collection<T> rv =  supertypes.get(uid);
        return rv != null ? rv : Collections.<T>emptySet();
    }
    
    public Collection<T> getSubproperties(T uid) {        
        Collection<T> rv =  subproperties.get(uid);
        return rv != null ? rv : Collections.singleton(uid);
    }

    public Collection<T> getSuperproperties(T uid) {
        Collection<T> rv =  superproperties.get(uid);
        return rv != null ? rv : Collections.<T>emptySet();
    }

}
