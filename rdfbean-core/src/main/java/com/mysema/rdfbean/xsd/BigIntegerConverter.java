/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.math.BigInteger;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * BigIntegerConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BigIntegerConverter extends AbstractConverter<BigInteger> {

    @Override
    public BigInteger fromString(String str) {
        return new BigInteger(str);
    }

    @Override
    public Class<BigInteger> getJavaType() {
        return BigInteger.class;
    }

    @Override
    public UID getType() {
        return XSD.integerType;
    }

}
