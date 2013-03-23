package com.mysema.rdfbean.tapestry;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ServiceD {

    void txMethod();

}
