package com.mysema.rdfbean.rdb;

import com.mysema.rdfbean.model.NODE;

/**
 * IDFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface IdFactory {

    Long getId(NODE node);

}