/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.IOException;

import javax.annotation.Nullable;

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
    @Nullable
    public Session getCurrentSession();

    /**
     * 
     */
    public void initialize();

    /**
     * 
     */
    public void close();
    
    /**
     * 
     * @param sessionContext
     */
    public void setSessionContext(SessionContext sessionContext);
    
    /**
     * @param <T>
     * @param cb
     * @return
     */
    public <T> T execute(SessionCallback<T> cb);
    
}
