/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import java.math.BigDecimal;

public class BigDecimalConverterTest extends AbstractConverterTest<BigDecimal>{

    @Override
    Converter<BigDecimal> createConverter() {
	return new BigDecimalConverter();
    }

    @Override
    BigDecimal createValue() {
	return BigDecimal.ONE;
    }

    
}
