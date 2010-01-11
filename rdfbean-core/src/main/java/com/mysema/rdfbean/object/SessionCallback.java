/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

/**
 * SessionCallback provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface SessionCallback<T> {
    
    @Nullable
    T doInSession(Session session);

}
