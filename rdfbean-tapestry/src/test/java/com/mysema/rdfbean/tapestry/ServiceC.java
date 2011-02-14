package com.mysema.rdfbean.tapestry;

import org.springframework.transaction.annotation.NotTransactional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ServiceC {

    void txMethod();

    @NotTransactional
    void nonTxMethod();

}