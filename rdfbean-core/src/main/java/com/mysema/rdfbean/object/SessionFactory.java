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
     * @param configuration
     * @return
     */
    public Session openSession(Configuration configuration);

    /**
     * 
     * @return
     */
    public Session getCurrentSession();

}
