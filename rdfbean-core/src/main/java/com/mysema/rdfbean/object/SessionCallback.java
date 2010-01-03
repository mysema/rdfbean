package com.mysema.rdfbean.object;

/**
 * SessionCallback provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface SessionCallback<T> {
    
    T doInSession(Session session);

}
