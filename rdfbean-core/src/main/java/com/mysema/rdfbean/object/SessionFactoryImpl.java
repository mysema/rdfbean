/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Locale;
import java.util.Map;

import com.mysema.rdfbean.annotations.Required;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

/**
 * AbstractSessionFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionFactoryImpl implements SessionFactory{

    private SessionContext sessionContext;
    
    private Configuration defaultConfiguration;
    
    private Repository<?> repository;
    
    private Locale locale;
    
    private Map<String, ObjectRepository> objectRepositories;

    public SessionFactoryImpl(){
        this(Locale.getDefault());
    }
    
    public SessionFactoryImpl(Locale locale) {
        setSessionContext(new EmptySessionContext());
    }

    public Session getCurrentSession(){
        return sessionContext.getCurrentSession();
    }
    
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Session openSession() {
        return openSession(defaultConfiguration);
    }

    @Override
    public Session openSession(Configuration configuration) {
        RDFConnection connection = repository.openConnection();
        SessionImpl session = new SessionImpl(configuration, connection, getLocale());
        if (objectRepositories != null) {
            for (Map.Entry<String, ObjectRepository> entry : objectRepositories.entrySet()) {
                session.addParent(entry.getKey(), entry.getValue());
            }
        }
        return session;
    }

    public void setDefaultConfiguration(Configuration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }
    
    public void setObjectRepositories(Map<String, ObjectRepository> objectRepositories) {
        this.objectRepositories = objectRepositories;
    }

    @Required
    public void setRepository(Repository<?> repository) {
        this.repository = repository;
    }

    public void setSessionContext(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }

}
