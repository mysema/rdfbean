/**
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Set;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author sasa
 *
 */
public class PredicateWildcardFetch implements FetchStrategy {
    
    private Set<UID> contexts = null;

    @Override
    public CloseableIterator<STMT> fetchStatements(RDFConnection connection,
            ID subject, UID predicate, NODE object, UID context,
            boolean includeInferred) {
        return connection.findStatements(subject, predicate, object, context, includeInferred);
    }

    @Override
    public Object getCacheKey(ID subject, UID predicate, NODE object,
            UID context, boolean includeInferred) {
        // NOTE: Inferred statements don't have reliable context information
        if (!includeInferred && (contexts == null || contexts.contains(context))) {
            return Arrays.asList(null, null, null, context, Boolean.FALSE);
        } else {
            return null;
        }
    }

}
