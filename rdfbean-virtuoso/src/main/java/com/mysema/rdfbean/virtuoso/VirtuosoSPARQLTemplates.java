package com.mysema.rdfbean.virtuoso;

import com.mysema.query.types.Ops;
import com.mysema.rdfbean.model.SPARQLTemplates;

public class VirtuosoSPARQLTemplates extends SPARQLTemplates {

    public static final SPARQLTemplates DEFAULT = new VirtuosoSPARQLTemplates();
    
    public VirtuosoSPARQLTemplates() {
        add(Ops.UPPER,           "bif:upper({0})");
        add(Ops.LOWER,           "bif:lower({0})");
        add(Ops.CONCAT,          "bif:concat({0},{1})");
        add(Ops.SUBSTR_1ARG,     "bif:substring({0},{1})");
        add(Ops.SUBSTR_2ARGS,    "bif:substring({0},{1},{2})");
        
        add(Ops.EXISTS,          "bif:exists({0})");
        
        add(Ops.LIKE,            "{0} like {1})");
    }
    
}
