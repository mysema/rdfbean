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
public interface SessionFactory {

    /**
     * 
     * @return
     */
    public Session openSession();

    /**
     * 
     * @return
     */
    public Session getCurrentSession();

    /**
     * 
     */
    public void initialize();

    /**
     * 
     */
    public void close();
    
}
