/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Format;

/**
 * @author tiwe
 * 
 */
@Immutable
public class RDFSource {

    @Nullable
    private final String resource;

    private final String context;

    private final Format format;

    @Nullable
    private final InputStream input;

    public RDFSource(String resource, Format format, String context) {
        this.input = null;
        this.resource = Assert.notNull(resource, "resource");
        this.format = Assert.notNull(format, "format");
        this.context = Assert.notNull(context, "context");
    }

    public RDFSource(InputStream input, Format format, String context) {
        this.input = Assert.notNull(input, "input");
        this.resource = null;
        this.format = Assert.notNull(format, "format");
        this.context = Assert.notNull(context, "context");
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
        if (input != null) {
            return input;
        } else if (resource == null) {
            throw new IllegalStateException();
        } else if (resource.startsWith("classpath:")) {
            String name = resource.substring(10);
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            return RDFSource.class.getClassLoader().getResourceAsStream(name);
        } else {
            return new URL(resource).openStream();
        }
    }

}
