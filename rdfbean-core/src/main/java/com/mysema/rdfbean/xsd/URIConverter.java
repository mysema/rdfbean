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

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


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
            throw new IllegalArgumentException(error, e);
        }
    }

    @Override
    public Class<URI> getJavaType() {
        return URI.class;
    }

    @Override
    public UID getType() {
        return XSD.anyURI;
    }

}
