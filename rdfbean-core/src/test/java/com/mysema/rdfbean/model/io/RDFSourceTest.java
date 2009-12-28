package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;


/**
 * RDFSourceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFSourceTest {
    
    @Test
    public void test() throws IOException{
        assertTrue(new RDFSource("classpath:/test.ttl", Format.TURTLE, "test:context").openStream() != null);
        assertTrue(new RDFSource("file:src/test/resources/test.ttl", Format.TURTLE, "test:context").openStream() != null);
        assertTrue(new RDFSource("classpath:/tes.ttl", Format.TURTLE, "test:context").openStream() == null);
    }

}
