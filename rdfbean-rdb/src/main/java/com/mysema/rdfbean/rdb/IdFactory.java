/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.Locale;

import com.mysema.rdfbean.model.NODE;

/**
 * IDFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface IdFactory {
    
    /**
     * @param locale
     * @return
     */
    Integer getId(Locale locale);

    /**
     * @param node
     * @return
     */
    Long getId(NODE node);

}