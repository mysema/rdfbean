/**
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Arrays;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author sasa
 *
 */
public class PredicateWildcardFetch implements FetchStrategy {
    
    private boolean includeInferred;

    @Override
    public CloseableIterator<STMT> fetchStatements(RDFConnection connection,
            ID subject, UID predicate, NODE object, UID context,
            boolean includeInferred) {
        return connection.findStatements(subject, null, null, context, includeInferred);
    }

    @Override
    public Object getCacheKey(ID subject, UID predicate, NODE object,
            UID context, boolean includeInferred) {
        if (subject != null && (this.includeInferred || !includeInferred)) {
            return Arrays.asList(subject, null, null, context, Boolean.valueOf(includeInferred));
        } else {
            return null;
        }
    }

}
