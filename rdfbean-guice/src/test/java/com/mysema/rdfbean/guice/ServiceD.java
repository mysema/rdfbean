package com.mysema.rdfbean.guice;

import org.springframework.transaction.annotation.Transactional;

/**
 * ServiceD provides
 *
 * @author tiwe
 * @version $Id$
 */
@Transactional
public interface ServiceD {

    void txMethod();
    
}
