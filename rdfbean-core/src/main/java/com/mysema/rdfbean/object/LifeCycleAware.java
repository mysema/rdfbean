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

    public void beforeBinding();
    
    public void afterBinding();

}
