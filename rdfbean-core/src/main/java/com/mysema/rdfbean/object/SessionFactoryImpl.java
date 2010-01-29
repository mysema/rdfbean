/*
 * Copyright (c) 2009 Mysema Ltd.
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
    
    private BID model;
    
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

    private void addCheckNumber(RDFConnection connection, BID model, BID bid) {
        String lid = configuration.getIdentityService().getLID(model, bid).getId();
        addStatement(connection, new STMT(bid, CORE.localId, new LIT(lid), null));
    }
    
    private void addStatement(RDFConnection connection, STMT stmt) {
        connection.update(Collections.<STMT>emptySet(), Collections.singleton(stmt));
    }

    private void cleanupModel(RDFConnection connection){
        removeStatements(connection, null, CORE.modelId, null, null);
        removeStatements(connection, null, CORE.localId, null, null);
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
        RDFConnection connection = repository.openConnection();
        try {
            CloseableIterator<STMT> statements = connection.findStatements(null, CORE.modelId, null, null, false);
            if (statements.hasNext()) {
                STMT statement = statements.next();
                if (!statements.hasNext()) {
                    BID subject = (BID) statement.getSubject();
                    BID object = (BID) statement.getObject();
                    model = (BID) object;
                    
                    if (verifyLocalId(connection, (BID) model, subject) && verifyLocalId(connection, model, object)) {
                        // OK
                        return;
                    }
                }
            }
            cleanupModel(connection);
            if (model == null) {
                // modelId
                BID subject = connection.createBNode();
                model = connection.createBNode();
                addStatement(connection, new STMT(subject, CORE.modelId, model, null));
    
                addCheckNumber(connection, model, subject);
                addCheckNumber(connection, model, model);
            }
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Session openSession() {
        RDFConnection connection = repository.openConnection();
        SessionImpl session = new SessionImpl(configuration, connection, getLocales(), model);
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

    private boolean verifyLocalId(RDFConnection connection, BID model, BID bnode) {
        String lid = configuration.getIdentityService().getLID(model, bnode).getId();
        CloseableIterator<STMT> stmts = connection.findStatements(bnode, CORE.localId, new LIT(lid), null, false);
        try {
            return stmts.hasNext();
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
