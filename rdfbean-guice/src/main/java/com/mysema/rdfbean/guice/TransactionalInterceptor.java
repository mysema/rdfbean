/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.guice;

import java.lang.reflect.Method;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SimpleSessionContext;
import com.mysema.rdfbean.object.TxException;


/**
 * TransactionalInterceptor provides a MethodInterceptor implementation for
 * transactional method interception
 *
 * @author tiwe
 * @version $Id$
 *
 */
@ThreadSafe
class TransactionalInterceptor implements MethodInterceptor{

    private final Provider<Map<Method,Transactional>> configuration;

    private SimpleSessionContext sessionContext;

    public TransactionalInterceptor(Provider<Map<Method,Transactional>> configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable { //NOSONAR
        Transactional annotation = configuration.get().get(methodInvocation.getMethod());
        boolean inSession = false;
        boolean inTx = false;
        if (sessionContext.getCurrentSession() != null){
            inSession = true;
            Session session = sessionContext.getCurrentSession();
            inTx = session.getTransaction() != null && session.getTransaction().isActive();
        }

        boolean intercepted = isIntercepted(annotation, inTx);
        if (!intercepted){
            return methodInvocation.proceed();
        }

        Session session = sessionContext.getOrCreateSession();
        FlushMode savedFlushMode = session.getFlushMode();

        try {
            RDFBeanTransaction txn = doBegin(session, annotation);
            Object result;
            try {
                result = methodInvocation.proceed();
            } catch(Exception e) {
                if (txn.isRollbackOnly() || isRollbackNecessary(annotation, e, txn)){
                    doRollback(txn);
                }else{
                    doCommit(session, txn);
                }
                throw e;
            }
            if (!txn.isRollbackOnly()){
                doCommit(session, txn);
            }else{
                doRollback(txn);
            }
            return result;

        } finally {
            session.setFlushMode(savedFlushMode);
            sessionContext.releaseSession();
            if (!inSession){
                session.close();
            }
        }
    }

    private boolean isIntercepted(Transactional annotation, boolean inTx) {
        switch(annotation.propagation()){
        case REQUIRED:
        case REQUIRES_NEW:
        case NESTED:
            if (inTx){
                return false;
            }
            break;

        case MANDATORY:
            if (inTx){
                return false;
            }else{
                throw new TxException("Tx propagation " + annotation.propagation() + " without transaction");
            }

        case NOT_SUPPORTED:
        case NEVER:
            if (inTx){
                throw new TxException("Tx propagation " + annotation.propagation() + " in transaction");
            }else{
                return false;
            }
        }
        return true;
    }

    private RDFBeanTransaction doBegin(Session session, Transactional transactional) {
        RDFBeanTransaction txn = session.beginTransaction(
                transactional.readOnly(),
                transactional.timeout(),
                transactional.isolation().value());

        session.setFlushMode(FlushMode.COMMIT);
        return txn;
    }

    private void doCommit(Session session, RDFBeanTransaction txn) throws Exception {
        Exception commitException = null;
        try {
            session.flush();
            txn.commit();

        } catch(RuntimeException re) {
            doRollback(txn);
            commitException = re;
        }

        if (commitException != null){
            throw commitException;
        }
    }

    private void doRollback(RDFBeanTransaction txn) {
        txn.rollback();
    }

    private boolean isRollbackNecessary(Transactional transactional, Exception e, RDFBeanTransaction txn) {
        boolean rollBack = false;
        for (Class<? extends Throwable> rollBackOn : transactional.rollbackFor()) {
            if (rollBackOn.isInstance(e)) {
                rollBack = true;
                for (Class<? extends Throwable> exceptOn : transactional.noRollbackFor()) {
                    if (exceptOn.isInstance(e)) {
                        rollBack = false;
                        break;
                    }
                }
                break;
            }
        }
        return rollBack;
    }

    @Inject
    public void setSessionFactory(SessionFactory sessionFactory){
        this.sessionContext = new SimpleSessionContext(sessionFactory);
        sessionFactory.setSessionContext(sessionContext);
    }

}
