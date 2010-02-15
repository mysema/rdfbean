/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.mysema.commons.lang.Assert;

import net.jcip.annotations.Immutable;

/**
 * RDFSource provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Immutable
public class RDFSource {

    private final String resource, context;

    private final Format format;

    public RDFSource(String resource, Format format, String context) {
        this.resource = Assert.notNull(resource);
        this.format = Assert.notNull(format);
        this.context = Assert.notNull(context);
    }

    public String getContext() {
        return context;
    }

    public Format getFormat() {
        return format;
    }

    public String getResource() {
        return resource;
    }
    
    public InputStream openStream() throws MalformedURLException, IOException {
        if (resource.startsWith("classpath:")){
            String name = resource.substring(10);
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            return RDFSource.class.getClassLoader().getResourceAsStream(name); 
        }else{
            return new URL(resource).openStream();
        }
    }

}
