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
    public Session openSession();

    /**
     * Get the current session
     * 
     * @return
     */
    @Nullable
    public Session getCurrentSession();

    /**
     * Initialize the SessionFactory
     */
    public void initialize();

    /**
     * Close the SessionFactory
     */
    public void close();
    
    /**
     * Set the SessionContext
     * 
     * @param sessionContext
     */
    public void setSessionContext(SessionContext sessionContext);
    
    /**
     * Execute the given SessionCallback in the scope of the current session or
     * a newly allocated, if no current is available
     * 
     * @param <T>
     * @param cb
     * @return
     */
    public <T> T execute(SessionCallback<T> cb);
    
}
