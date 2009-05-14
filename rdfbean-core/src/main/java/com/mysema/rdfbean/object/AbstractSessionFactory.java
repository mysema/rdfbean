/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * AbstractSessionFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractSessionFactory implements SessionFactory{

    private SessionContext sessionContext;
    
    public AbstractSessionFactory(){
        setSessionContext(new EmptySessionContext());
    }
    
    public Session getCurrentSession(){
        return sessionContext.getCurrentSession();
    }
    
    public void setSessionContext(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }
    
}
