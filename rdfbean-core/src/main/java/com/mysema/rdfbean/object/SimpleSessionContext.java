/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * SimpleSessionContext provides thread bound Session access
 * 
 * @author tiwe
 * @version $Id$
 */
public class SimpleSessionContext implements SessionContext {

    private final ThreadLocal<Session> sessionHolder = new ThreadLocal<Session>();

    private final SessionFactory sessionFactory;

    public SimpleSessionContext(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Session getCurrentSession() {
        return sessionHolder.get();
    }

    public Session getOrCreateSession() {
        if (sessionHolder.get() == null) {
            Session session = sessionFactory.openSession();
            sessionHolder.set(session);
            return session;
        } else {
            return sessionHolder.get();
        }
    }

    public void releaseSession() {
        sessionHolder.remove();
    }

}
