/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.math.BigDecimal;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * BigDecimalConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BigDecimalConverter extends AbstractConverter<BigDecimal> {

    @Override
    public BigDecimal fromString(String str) {
        return new BigDecimal(str);
    }

    @Override
    public Class<BigDecimal> getJavaType() {
        return BigDecimal.class;
    }

    @Override
    public UID getType() {
        return XSD.decimalType;
    }

}
