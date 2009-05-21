/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * LongConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LongConverter extends AbstractConverter<Long> {

    @Override
    public Long fromString(String str) {
        return Long.valueOf(str);
    }

}
