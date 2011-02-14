package com.mysema.rdfbean.tapestry;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface ServiceA {

    void nonTxMethod();

    @Transactional(propagation = Propagation.SUPPORTS)
    void nonTxMethod2();

    @Transactional
    void txMethod();

    @Transactional(propagation = Propagation.NEVER)
    void txMethod2();

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void txMethod3();

    @Transactional(readOnly = true)
    void txReadonly();

    @Transactional
    void txMethodWithException_commit() throws Exception;

    @Transactional(rollbackFor = Exception.class)
    void txMethodWithException_rollback() throws Exception;

}