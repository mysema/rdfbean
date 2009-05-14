/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;

public class JTASessionContext implements SessionContext {

    private static final Logger logger = LoggerFactory.getLogger(JTASessionContext.class);
    
    private SessionFactory sessionFactory;
    
    private TransactionManager transactionManager;
    
    private Map<Object,Session> currentSessionMap = new HashMap<Object,Session>();
    
    public JTASessionContext(SessionFactory sessionFactory, TransactionManager transactionManager) {
        this.sessionFactory = Assert.notNull(sessionFactory);
        this.transactionManager = Assert.notNull(transactionManager);
    }

    @Override
    public Session getCurrentSession() {
        Transaction txn;
        try {
            txn = transactionManager.getTransaction();
            if (txn == null) {
                throw new RuntimeException("Unable to locate current JTA transaction");
            }
        } catch (Throwable t) {
            throw new RuntimeException("Problem locating/validating JTA transaction", t);
        }

        Session currentSession = (Session) currentSessionMap.get(txn);

        if (currentSession == null) {
            currentSession = buildOrObtainSession();

            try {
                txn.registerSynchronization(buildCleanupSynch(txn));
            } catch (Throwable t) {
                try {
                    currentSession.close();
                } catch (Throwable ignore) {
                    logger.debug("Unable to release generated current-session on failed synch registration", ignore);
                }
                throw new RuntimeException(
                        "Unable to register cleanup Synchronization with TransactionManager");
            }

            currentSessionMap.put(txn, currentSession);
        }

        return currentSession;
    }

    /**
     * Builds a {@link CleanupSynch} capable of cleaning up the the current session map as an after transaction
     * callback.
     *
     * @param transactionIdentifier The transaction identifier under which the current session is registered.
     * @return The cleanup synch.
     */
    private CleanupSynch buildCleanupSynch(Transaction txn) {
        return new CleanupSynch( txn, this );
    }

    /**
     * Strictly provided for subclassing purposes; specifically to allow long-session
     * support.
     * <p/>
     * This implementation always just opens a new session.
     *
     * @return the built or (re)obtained session.
     */
    protected Session buildOrObtainSession() {
        return sessionFactory.openSession();
    }

    /**
     * JTA transaction synch used for cleanup of the internal session map.
     */
    protected static class CleanupSynch implements Synchronization {
        private Transaction txn;
        private JTASessionContext context;

        public CleanupSynch(Transaction txn, JTASessionContext context) {
            this.txn = txn;
            this.context = context;
        }

        /**
         * {@inheritDoc}
         */
        public void beforeCompletion() {
        }

        /**
         * {@inheritDoc}
         */
        public void afterCompletion(int i) {
            context.currentSessionMap.remove( txn );
        }
    }
}