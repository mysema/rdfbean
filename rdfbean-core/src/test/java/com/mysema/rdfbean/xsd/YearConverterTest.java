package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class YearConverterTest {

    private YearConverter converter = new YearConverter();

    @Test
    public void FromString() {
        assertEquals(new Year(2000), converter.fromString("2000"));
    }

    @Test
    public void GetJavaType() {
        assertEquals(Year.class, converter.getJavaType());
    }

}
