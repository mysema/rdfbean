/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.math.BigInteger;


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

}
