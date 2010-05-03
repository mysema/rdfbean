package com.mysema.rdfbean.xsd;

import java.net.MalformedURLException;
import java.net.URL;

public class URLConverterTest extends AbstractConverterTest<URL>{

    @Override
    Converter<URL> createConverter() {
	return new URLConverter();
    }

    @Override
    URL createValue() {
	try {
	    return new URL("http://test.com");
        } catch (MalformedURLException e) {
	    throw new IllegalArgumentException(e);
        }
    }

    
}
