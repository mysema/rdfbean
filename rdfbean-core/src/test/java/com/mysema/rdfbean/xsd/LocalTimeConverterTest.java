/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.LocalTime;

public class LocalTimeConverterTest extends AbstractConverterTest<LocalTime>{

    @Override
    Converter<LocalTime> createConverter() {
	return new LocalTimeConverter();
    }

    @Override
    LocalTime createValue() {
	LocalTime val = new LocalTime();
	return val.minusMillis(val.getMillisOfSecond());
    }

}
