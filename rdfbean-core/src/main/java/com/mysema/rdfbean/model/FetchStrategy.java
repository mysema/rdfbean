/**
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author sasa
 *
 */
public interface FetchStrategy {

    Object getCacheKey(ID subject, UID predicate, NODE object, UID context,
            boolean includeInferred);

    CloseableIterator<STMT> fetchStatements(RDFConnection connection, ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred);

}
