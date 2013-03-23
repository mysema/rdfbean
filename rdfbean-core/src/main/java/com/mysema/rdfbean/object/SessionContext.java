/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

/**
 * SessionContext provides thread bound Session access functionality
 * 
 * @author tiwe
 * @version $Id$
 */
public interface SessionContext {

    /**
     * 
     * @return
     */
    @Nullable
    Session getCurrentSession();

}
