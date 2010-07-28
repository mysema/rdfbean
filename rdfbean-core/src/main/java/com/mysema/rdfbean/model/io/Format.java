/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.io;

import java.io.OutputStream;

/**
 * Format provides
 *
 * @author tiwe
 * @version $Id$
 */
public enum Format {
    /**
     * 
     */
    N3("application/n3", TurtleWriter.class),
    /**
     * 
     */
    NTRIPLES("text/plain", NTriplesWriter.class),
    /**
     * 
     */
    RDFA("application/xhtml+xml", null),
    /**
     * 
     */
    RDFXML("application/rdf+xml", RDFXMLWriter.class),
    /**
     * 
     */
    TRIG("application/x-trig", TrigWriter.class),
    /**
     * 
     */
    TURTLE("text/turtle", TurtleWriter.class); // application/x-turtle
    
    private final String mimetype;
    
    private final Class<? extends RDFWriter> writerClass;
    
    private Format(String mime, Class<? extends RDFWriter> writerClass){
        this.mimetype = mime;
        this.writerClass = writerClass;
    }
    
    public String getMimetype(){
        return mimetype;
    }

    public RDFWriter createWriter(OutputStream out) {
        try {
            return writerClass.getConstructor(OutputStream.class).newInstance(out);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
