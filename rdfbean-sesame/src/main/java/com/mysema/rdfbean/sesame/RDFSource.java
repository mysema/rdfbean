/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.net.URL;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.Assert;

public class RDFSource {

    private URL resource;

    private RDFFormat format;

    private String context = "http://semantics.mysema.com/example#";

    public RDFSource() {
    }

    public RDFSource(URL resource) {
        this.resource = resource;
    }

    public RDFSource(URL resource, RDFFormat format, String context) {
        this.resource = resource;
        this.format = format;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public RDFFormat getFormat() {
        return format;
    }

    public URL getResource() {
        return resource;
    }

    public void setContext(String context) {
        this.context = Assert.notNull(context);
    }

    public void setFormat(RDFFormat format) {
        this.format = Assert.notNull(format);
    }

    public void setResource(URL resource) {
        this.resource = Assert.notNull(resource);
    }

    public void readInto(RepositoryConnection conn) throws RDFParseException,
            StoreException, IOException {
        if (format == null) {
            format = RDFFormat.forFileName(resource.getPath());
        }
        conn.add(resource.openStream(), context, format);
    }

}
