/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import org.springframework.transaction.annotation.Transactional;

public interface ServiceB {

    @Transactional
    void txMethod();

    @Transactional(readOnly = true)
    void txReadonly();

    void nonTxMethod();
}
