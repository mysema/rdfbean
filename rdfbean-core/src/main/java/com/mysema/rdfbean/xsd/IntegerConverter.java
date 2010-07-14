/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


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

    @Override
    public Class<Integer> getJavaType() {
        return Integer.class;
    }

    @Override
    public UID getType() {
        return XSD.intType;
    }

}
