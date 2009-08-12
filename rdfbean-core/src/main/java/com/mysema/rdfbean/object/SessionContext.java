/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

/**
 * SessionContext provides
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
