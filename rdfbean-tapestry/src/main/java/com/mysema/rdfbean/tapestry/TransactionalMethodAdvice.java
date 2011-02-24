/**
 *
 */
package com.mysema.rdfbean.tapestry;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleSessionContext;

/**
 * @author tiwe
 */
public final class TransactionalMethodAdvice implements MethodAdvice {

    private final TransactionalAdvisor transactionalAdvisor;

    private final SimpleSessionContext sessionContext;

    public TransactionalMethodAdvice(TransactionalAdvisor transactionalAdvisor, SimpleSessionContext sessionContext) {
        this.transactionalAdvisor = transactionalAdvisor;
        this.sessionContext = sessionContext;
    }

    @Override
    public void advise(Invocation invocation) {
        boolean inSession = false;
        boolean inTx = false;
        if (this.sessionContext.getCurrentSession() != null){
            inSession = true;
            Session session = sessionContext.getCurrentSession();
            inTx = session.getTransaction() != null && session.getTransaction().isActive();
        }

        if (!inTx){
            Session session = sessionContext.getOrCreateSession();
            FlushMode savedFlushMode = session.getFlushMode();

            try {
                RDFBeanTransaction txn = transactionalAdvisor.doBegin(session);
                try{
                    invocation.proceed();
                }catch(RuntimeException e){
                    transactionalAdvisor.doRollback(txn);
                    throw e;
                }
                // checked exception
                if (invocation.isFail()){
                    transactionalAdvisor.doRollback(txn);
                    invocation.rethrow();
                }
                if (!txn.isRollbackOnly()){
                    transactionalAdvisor.doCommit(session, txn);
                }else{
                    transactionalAdvisor.doRollback(txn);
                }

            } finally {
                session.setFlushMode(savedFlushMode);
                sessionContext.releaseSession();
                if (!inSession){
                    session.close();
                }
            }
        }else{
            invocation.proceed();
        }

    }
}