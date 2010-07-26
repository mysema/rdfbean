/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

public class EnumConverterTest extends AbstractConverterTest<EnumConverterTest.Values>{
    
    public enum Values{
	VAL1,
	VAL2
    }

    @Override
    EnumConverter<Values> createConverter() {
	return new EnumConverter<EnumConverterTest.Values>(Values.class);
    }

    @Override
    Values createValue() {
	return Values.VAL1;
    }

}
