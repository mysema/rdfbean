/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.sql.Connection;

import javax.annotation.Nullable;

/**
 * SessionCallback defines a callback interface for in-session logic
 * 
 * @author tiwe
 * @version $Id$
 */
public interface SQLConnectionCallback<T> {

    /**
     * @param session
     * @return
     */
    @Nullable
    T doInConnection(Connection connection);

}
