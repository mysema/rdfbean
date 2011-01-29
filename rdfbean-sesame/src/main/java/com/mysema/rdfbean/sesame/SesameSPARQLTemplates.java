package com.mysema.rdfbean.sesame;

import com.mysema.query.types.Ops;
import com.mysema.rdfbean.model.SPARQLTemplates;

public class SesameSPARQLTemplates extends SPARQLTemplates {

    public static final SPARQLTemplates DEFAULT = new SesameSPARQLTemplates();
    
    public SesameSPARQLTemplates() {
        add(Ops.TRIM,            "q:trim({0})");
        add(Ops.UPPER,           "q:upper({0})");
        add(Ops.LOWER,           "q:lower({0})");
        add(Ops.CONCAT,          "q:concat({0},{1})");
        add(Ops.SUBSTR_1ARG,     "q:substring({0},{1})");
        add(Ops.SUBSTR_2ARGS,    "q:substring2({0},{1},{2})");
        add(Ops.StringOps.SPACE, "q:space({0})");
        add(Ops.CHAR_AT,         "q:charAt({0},{1})");
        add(Ops.STARTS_WITH,     "q:startsWith({0},{1})");
        add(Ops.ENDS_WITH,       "q:endsWith({0},{1})");
        add(Ops.STARTS_WITH_IC,  "q:startsWithIc({0},{1})");
        add(Ops.ENDS_WITH_IC,    "q:endsWithIc({0},{1})");
        add(Ops.STRING_CONTAINS, "q:stringContains({0},{1})");
        add(Ops.STRING_CONTAINS_IC, "q:stringContainsIc({0},{1})");
        add(Ops.EQ_IGNORE_CASE,  "q:equalsIgnoreCase({0},{1})");
        add(Ops.STRING_LENGTH,   "q:stringLength({0})");
        add(Ops.INDEX_OF,        "q:indexOf({0},{1})");
        add(Ops.INDEX_OF_2ARGS,  "q:indexOf2({0},{1},{2})");
        add(Ops.MathOps.CEIL,    "q:ceil({0})");
        add(Ops.MathOps.FLOOR,   "q:floor({0})");
        add(Ops.MathOps.SQRT,    "q:sqrt({0})");
        add(Ops.MathOps.ABS,     "q:sqrt({0})");
        // TODO : xsd transformations
        add(Ops.DateTimeOps.YEAR,"q:year({0})");
        add(Ops.DateTimeOps.YEAR_MONTH,"q:yearMonth({0})");
        add(Ops.DateTimeOps.MONTH,"q:month({0})");
        add(Ops.DateTimeOps.WEEK,"q:week({0})");
        add(Ops.DateTimeOps.DAY_OF_WEEK,"q:dayOfWeek({0})");
        add(Ops.DateTimeOps.DAY_OF_MONTH,"q:dayOfMonth({0})");
        add(Ops.DateTimeOps.DAY_OF_YEAR,"q:dayOfYear({0})");
        add(Ops.DateTimeOps.HOUR,"q:hour({0})");
        add(Ops.DateTimeOps.MINUTE,"q:minute({0})");
        add(Ops.DateTimeOps.SECOND,"q:second({0})");
        add(Ops.DateTimeOps.MILLISECOND,"q:millisecond({0})");
        
        add(Ops.LIKE,            "q:like({0},{1})");
        add(Ops.MOD,             "q:like({0},{1})");
    }
    
}
