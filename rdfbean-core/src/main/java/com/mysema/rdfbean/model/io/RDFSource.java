/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.jcip.annotations.Immutable;

import com.mysema.commons.lang.Assert;

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
        this.resource = Assert.notNull(resource,"resource");
        this.format = Assert.notNull(format,"format");
        this.context = Assert.notNull(context,"context");
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
    
    public InputStream openStream() throws IOException {
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
