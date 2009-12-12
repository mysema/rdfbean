package com.mysema.rdfbean.xsd;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URLConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class URLConverter extends AbstractConverter<URL> {
    
    private static final Logger logger = LoggerFactory.getLogger(URLConverter.class);

    @Override
    public URL fromString(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

}