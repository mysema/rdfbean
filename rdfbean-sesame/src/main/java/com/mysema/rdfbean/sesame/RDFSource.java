/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.Assert;

// TODO : move this to rdfbean-core
public class RDFSource {

    private String resource;

    private RDFFormat format;

    private String context = "http://semantics.mysema.com/example#";

    public RDFSource() {
    }

    public RDFSource(String resource) {
        this.resource = resource;
    }

    public RDFSource(String resource, RDFFormat format, String context) {
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

    public void setContext(String context) {
        this.context = Assert.notNull(context);
    }

    public void setFormat(RDFFormat format) {
        this.format = Assert.notNull(format);
    }

    public void setResource(String resource) {
        this.resource = Assert.notNull(resource);
    }

    public void readInto(RepositoryConnection conn) throws RDFParseException,
            StoreException, IOException {
        if (format == null) {
            format = RDFFormat.forFileName(getResource());
        }
        conn.add(openStream(), context, format);
    }

}
