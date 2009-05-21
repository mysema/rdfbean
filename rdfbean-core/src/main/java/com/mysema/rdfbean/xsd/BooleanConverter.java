/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * BooleanConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BooleanConverter extends AbstractConverter<Boolean> {

    @Override
    public Boolean fromString(String str) {
        return Boolean.valueOf(str);
    }

}
