/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.mysema.rdfbean.model.Format;

public class RDFSourceTest {

    @Test
    public void OpenStream_with_Resource() throws IOException{
        assertTrue(new RDFSource("classpath:/test.ttl", Format.TURTLE, "test:context").openStream() != null);
        assertTrue(new RDFSource("file:src/test/resources/test.ttl", Format.TURTLE, "test:context").openStream() != null);
        assertTrue(new RDFSource("classpath:/tes.ttl", Format.TURTLE, "test:context").openStream() == null);
    }

    @Test
    public void OpenStream_with_Input() throws IOException{
        InputStream input = new ByteArrayInputStream("abc".getBytes("UTF-8"));
        RDFSource source = new RDFSource(input, Format.TURTLE, "test:context");
        assertEquals("abc", new String(ByteStreams.toByteArray(source.openStream()), Charsets.UTF_8));
    }

}
