package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collection;


public final class DCTERMS {

    public static final String NS = "http://purl.org/dc/terms/";
    
    public static final UID created = new UID(NS, "created");
    
    public static final UID modified = new UID(NS, "modified");
    
    private DCTERMS() {}
    
    public static final Collection<UID> ALL = Arrays.asList(created, modified);
    
}
