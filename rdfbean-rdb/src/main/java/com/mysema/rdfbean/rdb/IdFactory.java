/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.Locale;

import com.mysema.rdfbean.model.NODE;

/**
 * IDFactory defines id creation for NODE and Locale instances
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