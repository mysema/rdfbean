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

import com.google.common.collect.Multimap;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.MultimapFactory;

/**
 * AbstractOntology provides a generic implementation of the Ontology interface
 * 
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractOntology implements Ontology {

    private final Multimap<UID, UID> subtypes = MultimapFactory.<UID, UID> createWithSet();

    private final Multimap<UID, UID> supertypes = MultimapFactory.<UID, UID> createWithSet();

    private final Multimap<UID, UID> subproperties = MultimapFactory.<UID, UID> createWithSet();

    private final Multimap<UID, UID> superproperties = MultimapFactory.<UID, UID> createWithSet();

    protected void initializeTypeHierarchy(Set<UID> types,
            Multimap<UID, UID> directSubtypes,
            Multimap<UID, UID> directSupertypes) {
        for (UID type : types) {
            subtypes.put(type, type);
            if (directSubtypes.containsKey(type)) {
                flatten(type, directSubtypes, subtypes);
            }
            if (directSupertypes.containsKey(type)) {
                flatten(type, directSupertypes, supertypes);
            }
        }
    }

    protected void initializePropertyHierarchy(Set<UID> properties,
            Multimap<UID, UID> directSubproperties,
            Multimap<UID, UID> directSuperproperties) {
        for (UID property : properties) {
            subproperties.put(property, property);
            if (directSubproperties.containsKey(property)) {
                flatten(property, directSubproperties, subproperties);
            }
            if (directSuperproperties.containsKey(property)) {
                flatten(property, directSuperproperties, superproperties);
            }
        }
    }

    private void flatten(UID id, Multimap<UID, UID> direct, Multimap<UID, UID> expanded) {
        Stack<UID> t = new Stack<UID>();
        t.addAll(direct.get(id));
        while (!t.isEmpty()) {
            UID supertype = t.pop();
            expanded.put(id, supertype);
            if (direct.containsKey(supertype)) {
                t.addAll(direct.get(supertype));
            }
        }
    }

    public Collection<UID> getSubtypes(UID uid) {
        Collection<UID> rv = subtypes.get(uid);
        return rv != null ? rv : Collections.singleton(uid);
    }

    public Collection<UID> getSupertypes(UID uid) {
        Collection<UID> rv = supertypes.get(uid);
        return rv != null ? rv : Collections.<UID> emptySet();
    }

    public Collection<UID> getSubproperties(UID uid) {
        Collection<UID> rv = subproperties.get(uid);
        return rv != null ? rv : Collections.singleton(uid);
    }

    public Collection<UID> getSuperproperties(UID uid) {
        Collection<UID> rv = superproperties.get(uid);
        return rv != null ? rv : Collections.<UID> emptySet();
    }

}
