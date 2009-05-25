/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import com.mysema.commons.lang.Assert;

// TODO : get rid of Spring dependency
public class RDFSource {

	private Resource resource;
	
	private RDFFormat format;
	
	private String context = "http://semantics.mysema.com/example#";
	
	public RDFSource() {}

	public RDFSource(Resource resource) {
        this.resource = resource;
    }

    public RDFSource(Resource resource, RDFFormat format, String context) {
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

	public Resource getResource() {
		return resource;
	}

	@Required
	public void setContext(String context) {
		this.context = Assert.notNull(context);
	}

	public void setFormat(RDFFormat format) {
		this.format = Assert.notNull(format);
	}

	@Required
	public void setResource(Resource resource) {
		this.resource = Assert.notNull(resource);
	}
	
	public void readInto(RepositoryConnection conn) throws RDFParseException, StoreException, IOException {
        if (format == null) {
            format = RDFFormat.forFileName(resource.getFilename());
        }
        conn.add(resource.getInputStream(), context, format);
	}
	
}
