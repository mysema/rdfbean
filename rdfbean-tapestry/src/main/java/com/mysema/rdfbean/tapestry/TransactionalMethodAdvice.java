/**
 * 
 */
package com.mysema.rdfbean.tapestry;

import java.io.IOException;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleSessionContext;
import com.mysema.rdfbean.object.TxException;

/**
 * @author tiwe
 *
 */
public final class TransactionalMethodAdvice implements MethodAdvice {
    
    private final TransactionalAdvisor transactionalAdvisor;

    private final SimpleSessionContext sessionContext;
    
    TransactionalMethodAdvice(TransactionalAdvisor transactionalAdvisor, SimpleSessionContext sessionContext) {
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
                try {
                    invocation.proceed();                
                } catch(Exception e) {                
                    if (txn.isRollbackOnly()){
                        transactionalAdvisor.doRollback(txn);
                    }else{
                        transactionalAdvisor.doCommit(session, txn);
                    }
                    throw new RuntimeException(e);
                }
                transactionalAdvisor.doCommit(session, txn);
                
            } finally {
                session.setFlushMode(savedFlushMode);
                sessionContext.releaseSession();
                if (!inSession){
                    try {
                        session.close();
                    } catch (IOException e) {
                        String error = "Caught " + e.getClass().getName();
                        TransactionalAdvisorImpl.logger.error(error, e);
                        throw new TxException(error, e);
                    }
                }                                    
            }    
        }else{
            invocation.proceed();
        }                   
        
    }
}