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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * SessionFactoryImpl is the default implementation of the SessionFactory interface
 *
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class SessionFactoryImpl implements SessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactoryImpl.class);
    
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
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
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
                    String error = "Caught " + e.getClass().getName();
                    logger.error(error, e);
                    throw new RuntimeException(error, e);
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

    private void removeStatement(RDFConnection connection, STMT stmt) {
        connection.update(Collections.singleton(stmt), Collections.<STMT>emptySet());
    }

    protected void removeStatements(RDFConnection connection,@Nullable ID subject, UID predicate, @Nullable NODE object, @Nullable UID context) {
        CloseableIterator<STMT> stmts = connection.findStatements(subject, predicate, object, context, false);
        try {
            while (stmts.hasNext()) {
                removeStatement(connection, stmts.next());
            }
        } finally {
            try {
                stmts.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

    public void setSessionContext(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }

}
