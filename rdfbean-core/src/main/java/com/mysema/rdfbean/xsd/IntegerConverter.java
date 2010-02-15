/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * IntegerConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IntegerConverter  extends AbstractConverter<Integer> {

    @Override
    public Integer fromString(String str) {
        return Integer.valueOf(str);
    }

}
