package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class LocaleConverterTest {

    private LocaleConverter converter = new LocaleConverter();
    
    @Test
    public void FromString() {
        assertEquals(Locale.ENGLISH, converter.fromString("en"));
    }

    @Test
    public void ToStringLocale() {
        assertEquals("en", converter.toString(Locale.ENGLISH));
    }

}
