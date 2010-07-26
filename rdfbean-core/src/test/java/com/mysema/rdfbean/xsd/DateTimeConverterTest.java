/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTime;

public class DateTimeConverterTest extends AbstractConverterTest<DateTime>{

    @Override
    Converter<DateTime> createConverter() {
	return new DateTimeConverter();
    }

    @Override
    DateTime createValue() {
	DateTime val = new DateTime();
	return val.minusMillis(val.getMillisOfSecond());
    }

}
