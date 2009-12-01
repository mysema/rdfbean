package com.mysema.rdfbean.tapestry;

import java.lang.reflect.Method;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SimpleSessionContext;

/**
 * TransactionalWorker provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public class TransactionalAdvisorImpl implements TransactionalAdvisor {
    
    private final MethodAdvice advice = new MethodAdvice() {
        @Override
        public void advise(Invocation invocation) {
            boolean inTx = false;
            if (sessionContext.getCurrentSession() != null){
                Session session = sessionContext.getCurrentSession();
                inTx = session.getTransaction() != null && session.getTransaction().isActive();
            }
            
            if (!inTx){
                Session session = sessionContext.getOrCreateSession();
                FlushMode savedFlushMode = session.getFlushMode();

                try {
                    RDFBeanTransaction txn = doBegin(session);  
                    try {
                        invocation.proceed();                
                    } catch(Exception e) {                
                        if (txn.isRollbackOnly()){
                            doRollback(txn);
                        }else{
                            doCommit(session, txn);
                        }
                        throw new RuntimeException(e);
                    }
                    doCommit(session, txn);
                    
                } finally {
                    session.setFlushMode(savedFlushMode);
                    sessionContext.releaseSession();                
                }    
            }else{
                invocation.proceed();
            }                   
            
        }
    };
    
    private RDFBeanTransaction doBegin(Session session) {
        RDFBeanTransaction txn = session.beginTransaction(
                false, // not readonly 
                -1,    // default timeout
                -1);   // default isolation
        
        session.setFlushMode(FlushMode.COMMIT);
        return txn;
    }
    
    private void doCommit(Session session, RDFBeanTransaction txn){
        RuntimeException commitException = null;
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

    private final SimpleSessionContext sessionContext;

    public TransactionalAdvisorImpl(SessionFactory sessionFactory) {
        this.sessionContext = new SimpleSessionContext(sessionFactory);
        sessionFactory.setSessionContext(sessionContext);
    }

    @SuppressWarnings("unchecked")
    public void addTransactionCommitAdvice(MethodAdviceReceiver receiver) {
        if (receiver.getInterface().getAnnotation(Transactional.class) != null){
            for (Method m : receiver.getInterface().getMethods()) {
                receiver.adviseMethod(m, advice);
            }
        }else{
            for (Method m : receiver.getInterface().getMethods()) {
                if (m.getAnnotation(Transactional.class) != null) {
                    receiver.adviseMethod(m, advice);
                }
            }    
        }
        
    }
}
