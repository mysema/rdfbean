/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import java.util.Collections;
import java.util.Set;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * EmptyOntology provides an empty implementation of the Ontology interface
 * This implementation is safe to use but returns empty sets for superclass and superproperty queries
 * and only the type/property itself for subclass and subproperty queries.
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public final class EmptyOntology implements Ontology{

    private EmptyOntology(){}
    
    public static final Ontology DEFAULT = new EmptyOntology();
    
    @Override
    public Set<UID> getSubtypes(UID uid) {
        return Collections.singleton(uid);
    }

    @Override
    public Set<UID> getSupertypes(UID uid) {
        return Collections.emptySet();
    }

    @Override
    public Set<UID> getSubproperties(UID uid) {
        return Collections.singleton(uid);
    }

    @Override
    public Set<UID> getSuperproperties(UID uid) {
        return Collections.emptySet();
    }

    
}
