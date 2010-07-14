/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * DoubleConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DoubleConverter extends AbstractConverter<Double> {

    @Override
    public Double fromString(String str) {
        return Double.valueOf(str);
    }

    @Override
    public Class<Double> getJavaType() {
        return Double.class;
    }

    @Override
    public UID getType() {
        return XSD.doubleType;
    }

}
