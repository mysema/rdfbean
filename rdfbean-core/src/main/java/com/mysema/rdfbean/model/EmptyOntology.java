package com.mysema.rdfbean.model;

import java.util.Collections;
import java.util.Set;

/**
 * EmptyOntology provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class EmptyOntology implements Ontology{

    @Override
    public Set<UID> getSubtypes(UID uid) {
        return Collections.singleton(uid);
    }

    @Override
    public Set<UID> getSupertypes(UID uid) {
        return Collections.emptySet();
    }

    
}
