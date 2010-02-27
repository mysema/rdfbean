/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

/**
 * Creates Sessions and provides access to the thread bound session
 * 
 * @author sasa
 * 
 */
public interface SessionFactory {

    /**
     * Create a backend connection and open a Session for it
     * 
     * @return
     */
    Session openSession();

    /**
     * Get the current session
     * 
     * @return
     */
    @Nullable
    Session getCurrentSession();

    /**
     * Initialize the SessionFactory
     */
    void initialize();

    /**
     * Close the SessionFactory
     */
    void close();
    
    /**
     * Set the SessionContext
     * 
     * @param sessionContext
     */
    void setSessionContext(SessionContext sessionContext);
    
    /**
     * Execute the given SessionCallback in the scope of the current session or
     * a newly allocated, if no current is available
     * 
     * @param <T>
     * @param cb
     * @return
     */
    <T> T execute(SessionCallback<T> cb);
    
}
