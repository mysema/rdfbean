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

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.*;

/**
 * SessionFactoryImpl is the default implementation of the SessionFactory interface
 *
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class SessionFactoryImpl implements SessionFactory {

    private SessionContext sessionContext;
    
    private Configuration configuration;
    
    private BID model;
    
    private Repository repository;
    
    private Iterable<Locale> locales;
    
    private Map<String, ObjectRepository> objectRepositories;

    public SessionFactoryImpl(){
        this(Locale.getDefault());
    }
    
    public SessionFactoryImpl(Locale locale) {
        this(Collections.singleton(locale));
        setSessionContext(new EmptySessionContext());
    }

    public SessionFactoryImpl(Iterable<Locale> locales) {
        this.locales = locales;
    }

    public Session getCurrentSession(){
        return sessionContext.getCurrentSession();
    }
    
    public Iterable<Locale> getLocales() {
        return locales;
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

    private void removeStatement(RDFConnection connection, STMT stmt) {
        connection.update(Collections.singleton(stmt), Collections.<STMT>emptySet());
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
    
    public void setObjectRepositories(Map<String, ObjectRepository> objectRepositories) {
        this.objectRepositories = objectRepositories;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setSessionContext(SessionContext sessionContext){
        this.sessionContext = sessionContext;
    }

    public void setLocale(Locale locale) {
        setLocales(Collections.singleton(locale));
    }

    public void setLocales(Iterable<Locale> locales) {
        this.locales = locales;
    }

    @Override
    public void close() {
        repository.close();
    }

}
