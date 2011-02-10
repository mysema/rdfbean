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
        add(Ops.TRIM,            "bif:trim({0})");
        add(Ops.EQ_IGNORE_CASE,  "bif:lower({0}) = bif:lower({1})");
        add(Ops.STRING_IS_EMPTY, "{0} like ''");
        add(Ops.LIKE,            "{0} like {1}");
        add(Ops.STRING_LENGTH,   "bif:string-length({0})");
        
        add(Ops.EXISTS,          "bif:exists ((select * where {0}))");
        
        add(Ops.MOD,             "bif:mod({0},{1})");
        add(Ops.MathOps.ABS,     "bif:abs({0})");
        add(Ops.MathOps.SQRT,    "bif:sqrt({0})");
    }
    
}
