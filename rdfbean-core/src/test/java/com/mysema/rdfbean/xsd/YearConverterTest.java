package com.mysema.rdfbean.xsd;

public class YearConverterTest extends AbstractConverterTest<Year>{

    @Override
    Converter<Year> createConverter() {
	return new YearConverter();
    }

    @Override
    Year createValue() {
	return new Year(100);
    }

}
