package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collection;


public final class DC {

    public static final String NS = "http://purl.org/dc/elements/1.1/";
    
    public static final UID title = new UID(NS, "title");
    
    public static final UID description = new UID(NS, "description");
    
    public static final UID identifier = new UID(NS, "identifier");
    
    public static final UID contributor = new UID(NS, "contributor");
    
    public static final UID coverage = new UID(NS, "coverage");
    
    public static final UID creator = new UID(NS, "creator");
    
    public static final UID date = new UID(NS, "date");
    
    public static final UID format = new UID(NS, "format");
    
    public static final UID language = new UID(NS, "language");
    
    public static final UID publisher = new UID(NS, "publisher");
    
    public static final UID relation = new UID(NS, "relation");
    
    public static final UID rights = new UID(NS, "rights");
    
    public static final UID source = new UID(NS, "source");
    
    public static final UID subject = new UID(NS, "subject");
    
    public static final UID type = new UID(NS, "type");
    
    private DC() {}
    
    public static final Collection<UID> ALL = Arrays.asList(title, description, identifier);
}
