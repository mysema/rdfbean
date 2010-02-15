/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * URIConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class URIConverter  extends AbstractConverter<URI> {

    private static final Logger logger = LoggerFactory.getLogger(URIConverter.class);
    
    @Override
    public URI fromString(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

}
