/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

/**
 * SessionCallback defines a callback interface for in-session logic
 *
 * @author tiwe
 * @version $Id$
 */
public interface SessionCallback<T> {
    
    /**
     * @param session
     * @return
     */
    @Nullable
    T doInSession(Session session);

}
