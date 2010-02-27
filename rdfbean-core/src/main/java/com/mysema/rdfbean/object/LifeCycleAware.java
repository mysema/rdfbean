/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * @author sasa
 *
 */
public interface LifeCycleAware {

    /**
     * 
     */
    void beforeBinding();
    
    /**
     * 
     */
    void afterBinding();

}
