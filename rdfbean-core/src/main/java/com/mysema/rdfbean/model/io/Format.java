package com.mysema.rdfbean.model.io;

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
    N3("application/n3"),
    /**
     * 
     */
    NTRIPLES(null),
    /**
     * 
     */
    RDFA("application/xhtml+xml"),
    /**
     * 
     */
    RDFXML("application/rdf+xml"),
    /**
     * 
     */
    TRIG("application/x-trig"),
    /**
     * 
     */
    TURTLE("text/turtle"); // application/x-turtle
    
    private final String mimetype;
    
    private Format(String mime){
        this.mimetype = mime;
    }
    
    public String getMimetype(){
        return mimetype;
    }
}
