/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;

/**
 * TransactionalAdvisor provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface TransactionalAdvisor {

    /**
     * @param receiver
     */
    void addTransactionCommitAdvice(MethodAdviceReceiver receiver);

}