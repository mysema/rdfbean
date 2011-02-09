package com.mysema.rdfbean.model;

import com.mysema.query.types.Ops;
import com.mysema.query.types.PathType;
import com.mysema.query.types.Templates;

public class SPARQLTemplates extends Templates{

    public static final SPARQLTemplates DEFAULT = new SPARQLTemplates(); 
    
    public SPARQLTemplates() {
        add(PathType.VARIABLE,   "?{0s}");
        add(Ops.IS_NOT_NULL,     "bound({0})");
        add(Ops.IS_NULL,         "!bound({0})");
        
        add(Ops.MATCHES,         "regex(str({0}), {1s})");
        add(Ops.STARTS_WITH,     "regex({0}, '^{1s}')");
        add(Ops.ENDS_WITH,       "regex({0}, '{1s}$')");
        add(Ops.STRING_CONTAINS, "regex({0}, '.*{1s}.*')");
        add(Ops.MATCHES_IC,         "regex(str({0}), {1s}, 'i')");
        add(Ops.STARTS_WITH_IC,  "regex({0}, '^{1s}','i')");
        add(Ops.ENDS_WITH_IC,    "regex({0}, '{1s}$','i')");
        add(Ops.STRING_CONTAINS_IC,"regex({0}, '.*{1s}.*','i')");
        
        add(Ops.EXISTS,          "exists({0})");
    }
    
}