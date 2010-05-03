package com.mysema.rdfbean.xsd;

import java.net.URI;
import java.net.URISyntaxException;

public class URIConverterTest extends AbstractConverterTest<URI>{

    @Override
    Converter<URI> createConverter() {
	return new URIConverter();
    }

    @Override
    URI createValue() {
	try {
	    return new URI("http://test.com");
        } catch (URISyntaxException e) {
	    throw new IllegalArgumentException(e);
        }
    }

    
}
