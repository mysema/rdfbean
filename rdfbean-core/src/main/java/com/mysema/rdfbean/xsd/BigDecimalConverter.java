/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.math.BigDecimal;


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

}
