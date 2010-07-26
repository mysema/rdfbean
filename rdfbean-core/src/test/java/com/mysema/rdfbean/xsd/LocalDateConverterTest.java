/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.LocalDate;

public class LocalDateConverterTest extends AbstractConverterTest<LocalDate>{

    @Override
    Converter<LocalDate> createConverter() {
	return new LocalDateConverter();
    }

    @Override
    LocalDate createValue() {
	return new LocalDate();
    }

}
