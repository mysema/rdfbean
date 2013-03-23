package com.mysema.rdfbean.guice;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ServiceD {

    void txMethod();

}
