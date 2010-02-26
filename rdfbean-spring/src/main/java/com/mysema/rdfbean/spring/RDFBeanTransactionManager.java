/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.spring;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.object.SimpleSessionContext;


/**
 * 
 * RDFBeanTransactionManager is a PlatformTransactionManager implementation for RDFBean usage in Spring
 *
 * @author tiwe
 * @version $Id$
 *
 */
public class RDFBeanTransactionManager extends AbstractPlatformTransactionManager{

    private static final Logger logger = LoggerFactory.getLogger(RDFBeanTransactionManager.class);
    
    private static final long serialVersionUID = -4060513400839374983L;

    private transient final SimpleSessionContext sessionContext;
        
    private boolean clearSessionOnRollback = false;

    /**
     * Create a new RDFBeanTransactionManager instance.
     */
    public RDFBeanTransactionManager(SessionFactoryImpl sessionFactory) {
        this.sessionContext = new SimpleSessionContext(sessionFactory);
        sessionFactory.setSessionContext(sessionContext);
        setRollbackOnCommitFailure(false);
    }

    /**
     * Return a transaction object for the current transaction state.
     * <p>The returned object will usually be specific to the concrete transaction
     * manager implementation, carrying corresponding transaction state in a
     * modifiable fashion. This object will be passed into the other template
     * methods (e.g. doBegin and doCommit), either directly or as part of a
     * DefaultTransactionStatus instance.
     * <p>The returned object should contain information about any existing
     * transaction, that is, a transaction that has already started before the
     * current <code>getTransaction</code> call on the transaction manager.
     * Consequently, a <code>doGetTransaction</code> implementation will usually
     * look for an existing transaction and store corresponding state in the
     * returned transaction object.
     * @return the current transaction object
     * @throws org.springframework.transaction.CannotCreateTransactionException
     * if transaction support is not available
     * @throws TransactionException in case of lookup or system errors
     * @see #doBegin
     * @see #doCommit
     * @see #doRollback
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected Object doGetTransaction() {
        boolean closeAfterTx = sessionContext.getCurrentSession() == null;
        Session session = sessionContext.getOrCreateSession();               
        return new TransactionObject(session, closeAfterTx);
    }

    /**
     * Begin a new transaction with semantics according to the given transaction
     * definition. Does not have to care about applying the propagation behavior,
     * as this has already been handled by this abstract manager.
     * <p>This method gets called when the transaction manager has decided to actually
     * start a new transaction. Either there wasn't any transaction before, or the
     * previous transaction has been suspended.
     * <p>A special scenario is a nested transaction without savepoint: If
     * <code>useSavepointForNestedTransaction()</code> returns "false", this method
     * will be called to start a nested transaction when necessary. In such a context,
     * there will be an active transaction: The implementation of this method has
     * to detect this and start an appropriate nested transaction.
     * @param transaction transaction object returned by <code>doGetTransaction</code>
     * @param definition TransactionDefinition instance, describing propagation
     * behavior, isolation level, read-only flag, timeout, and transaction name
     * @throws TransactionException in case of creation or system errors
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        try {            
            // session
            Session s = ((TransactionObject) transaction).getSession();
            s.beginTransaction(
                    definition.isReadOnly(), 
                    determineTimeout(definition),
                    definition.getIsolationLevel());
            
            s.setFlushMode(FlushMode.COMMIT);
            
        } catch (RuntimeException oe) {
            throw new TransactionSystemException("error beginning transaction", oe);
        }
    }

    /**
     * Perform an actual commit of the given transaction.
     * <p>An implementation does not need to check the "new transaction" flag
     * or the rollback-only flag; this will already have been handled before.
     * Usually, a straight commit will be performed on the transaction object
     * contained in the passed-in status.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of commit or system errors
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {        
        TransactionObject txObj = (TransactionObject) status.getTransaction();
        RDFBeanTransaction tx = txObj.getTransaction();
        if (tx == null){
            throw new TransactionUsageException("no transaction active");
        }
        
        try {     
            txObj.getSession().flush();            
            tx.commit();            
        } catch (RuntimeException oe) {
            throw new TransactionSystemException("error committing transaction", oe);
        } finally {
            if (txObj.getOriginalFlushMode() != null){
                txObj.getSession().setFlushMode(txObj.getOriginalFlushMode());    
            }            
            sessionContext.releaseSession();
            txObj.close();
        }
    }

    /**
     * Perform an actual rollback of the given transaction.
     * <p>An implementation does not need to check the "new transaction" flag;
     * this will already have been handled before. Usually, a straight rollback
     * will be performed on the transaction object contained in the passed-in status.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of system errors
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        TransactionObject txObj = (TransactionObject) status.getTransaction();
        RDFBeanTransaction tx = txObj.getTransaction();
        if (tx == null){
            throw new TransactionUsageException("no transaction active");
        }
        try {            
            tx.rollback();
        } catch (RuntimeException oe) {
            throw new TransactionSystemException("error rolling back transaction", oe);
        } finally {
            if (txObj.getOriginalFlushMode() != null){
                txObj.getSession().setFlushMode(txObj.getOriginalFlushMode());    
            }            
            sessionContext.releaseSession();
            if (clearSessionOnRollback) {
                txObj.getSession().clear();
            }            
            txObj.close();
        }
    }

    /**
     * Check if the given transaction object indicates an existing transaction
     * (that is, a transaction which has already started).
     * <p>The result will be evaluated according to the specified propagation
     * behavior for the new transaction. An existing transaction might get
     * suspended (in case of PROPAGATION_REQUIRES_NEW), or the new transaction
     * might participate in the existing one (in case of PROPAGATION_REQUIRED).
     * <p>The default implementation returns <code>false</code>, assuming that
     * participating in existing transactions is generally not supported.
     * Subclasses are of course encouraged to provide such support.
     * @param transaction transaction object returned by doGetTransaction
     * @return if there is an existing transaction
     * @throws TransactionException in case of system errors
     * @see #doGetTransaction
     */
    @Override
    protected boolean isExistingTransaction(Object transaction) {        
        return ((TransactionObject) transaction).getTransaction() != null &&
            ((TransactionObject)transaction).getTransaction().isActive(); 
    }

    /**
     * Set the given transaction rollback-only. Only called on rollback
     * if the current transaction participates in an existing one.
     * <p>The default implementation throws an IllegalTransactionStateException,
     * assuming that participating in existing transactions is generally not
     * supported. Subclasses are of course encouraged to provide such support.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of system errors
     */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        TransactionObject txObj = (TransactionObject) status.getTransaction();
        try {
            txObj.setRollbackOnly();
        } catch (RuntimeException oe) {
            throw new TransactionSystemException("error setting rollback-only",oe);
        }
    }

    /**
     * Set the clear-session-on-rollback flag. Because after a rollback the state of the objects in
     * the session does not match the state of the database, it's usually prudent to clear the
     * session after a rollback to prevent the application from continuing with stale objects.
     *
     * <p>Changing this flag will affect the current transaction, if there is one.
     *
     * @param clearSessionOnRB true if the session should be <code>clear()</code>'d on transaction
     *                         rollback
     */
    public void setClearSessionOnRollback(boolean clearSessionOnRB) {
        this.clearSessionOnRollback = clearSessionOnRB;
    }

    /**
     * Implement SmartTransactionObject so spring can do proper rollback-only handling.
     */
    private static class TransactionObject implements SmartTransactionObject {
        private final Session session;
        
        private final boolean closeAfterTx;

        private FlushMode flushMode;
        
        public TransactionObject(Session session, boolean closeAfterTx) {
            this.session = session;
            this.closeAfterTx = closeAfterTx;
            this.flushMode = session.getFlushMode();
        }

        public FlushMode getOriginalFlushMode(){
            return flushMode;
        }
        
        public Session getSession() {
            return session;
        }
        
        public RDFBeanTransaction getTransaction(){
            return session.getTransaction();
        }

        public void setRollbackOnly() {
            session.getTransaction().setRollbackOnly();
        }

        public boolean isRollbackOnly() {
            return session.getTransaction().isRollbackOnly();
        }

        public void close(){
            if (closeAfterTx){
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
    
}
