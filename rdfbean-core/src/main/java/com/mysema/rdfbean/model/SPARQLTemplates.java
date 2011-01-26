package com.mysema.rdfbean.model;

import com.mysema.query.types.Ops;
import com.mysema.query.types.PathType;
import com.mysema.query.types.Templates;

public class SPARQLTemplates extends Templates{

    public static final SPARQLTemplates DEFAULT = new SPARQLTemplates(); 
    
    public SPARQLTemplates() {
        add(PathType.VARIABLE, "?{0s}");
        add(Ops.IS_NOT_NULL,   "bound({0})");
        add(Ops.IS_NULL,       "!bound({0})");
    }
    
}