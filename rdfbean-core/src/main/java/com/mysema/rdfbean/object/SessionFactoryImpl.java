/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

/**
 * SessionFactoryImpl is the default implementation of the SessionFactory interface
 *
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class SessionFactoryImpl implements SessionFactory {

    private Configuration configuration;
    
    private Iterable<Locale> locales;
    
    private Map<String, ObjectRepository> objectRepositories;
    
    private Repository repository;
    
    private SessionContext sessionContext;

    public SessionFactoryImpl(){
        this(Locale.getDefault());
    }
    
    public SessionFactoryImpl(Iterable<Locale> locales) {
        this.locales = locales;
    }

    public SessionFactoryImpl(Locale locale) {
        this(Collections.singleton(locale));
        setSessionContext(new EmptySessionContext());
    }

    @Override
    public void close() {
        repository.close();        
    }

    @Override
    public <T> T execute(SessionCallback<T> cb){
        if (sessionContext.getCurrentSession() != null){
            return cb.doInSession(sessionContext.getCurrentSession());
        }else{
            Session session = openSession();
            try{
                return cb.doInSession(session);
            }finally{
                try {
                    session.close();
                } catch (IOException e) {
                    throw new SessionException(e);
                }
            }
        }
    }
    
    public Session getCurrentSession(){
        return sessionContext.getCurrentSession();
    }

    public Iterable<Locale> getLocales() {
        return locales;
    }

    public void initialize() {        
        repository.initialize();        
    }

    @Override
    public Session openSession() {
        RDFConnection connection = repository.openConnection();
        SessionImpl session = new SessionImpl(configuration, connection, getLocales());
        if (objectRepositories != null) {
            for (Map.Entry<String, ObjectRepository> entry : objectRepositories.entrySet()) {
                session.addParent(entry.getKey(), entry.getValue());
            }
        }
        return session;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setLocale(Locale locale) {
        setLocales(Collections.singleton(locale));
    }

    public void setLocales(Iterable<Locale> locales) {
        this.locales = locales;
    }

    public void setObjectRepositories(Map<String, ObjectRepository> objectRepositories) {
        this.objectRepositories = objectRepositories;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public final void setSessionContext(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }

}
