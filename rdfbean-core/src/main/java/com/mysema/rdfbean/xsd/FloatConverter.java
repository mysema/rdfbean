/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * FloatConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FloatConverter extends AbstractConverter<Float> {

    @Override
    public Float fromString(String str) {
        return Float.valueOf(str);
    }

    @Override
    public Class<Float> getJavaType() {
        return Float.class;
    }

    @Override
    public UID getType() {
        return XSD.floatType;
    }

}
