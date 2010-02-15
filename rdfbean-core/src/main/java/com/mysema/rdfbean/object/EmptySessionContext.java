/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * EmptySessionContext is an empty implementation of the SessionContext interface
 *
 * @author tiwe
 * @version $Id$
 */
public final class EmptySessionContext implements SessionContext{

    @Override
    public Session getCurrentSession() {
        return null;
    }

}
