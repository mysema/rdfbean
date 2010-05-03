/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;

/**
 * TransactionalAdvisor provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface TransactionalAdvisor {

    void addTransactionCommitAdvice(MethodAdviceReceiver receiver);
    
    RDFBeanTransaction doBegin(Session session);
    
    void doCommit(Session session, RDFBeanTransaction txn);

    void doRollback(RDFBeanTransaction txn);

}