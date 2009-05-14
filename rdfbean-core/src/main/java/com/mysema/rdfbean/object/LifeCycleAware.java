/*
 * Copyright (c) 2009 Mysema Ltd.
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
