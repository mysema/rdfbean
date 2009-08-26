/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import com.mysema.rdfbean.guice.tx.Transactional;

/**
 * ServiceB provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface ServiceB {

    @Transactional
    void txMethod();

    @Transactional(readOnly=true)
    void txReadonly();
    
    void nonTxMethod();
}
