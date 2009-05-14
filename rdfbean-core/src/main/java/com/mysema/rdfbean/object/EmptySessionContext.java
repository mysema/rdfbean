/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * EmptySessionContext provides
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
