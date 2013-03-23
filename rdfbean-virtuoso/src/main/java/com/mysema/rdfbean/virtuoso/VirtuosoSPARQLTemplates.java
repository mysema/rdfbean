package com.mysema.rdfbean.virtuoso;

import com.mysema.query.types.Ops;
import com.mysema.rdfbean.model.SPARQLTemplates;

public class VirtuosoSPARQLTemplates extends SPARQLTemplates {

    public static final SPARQLTemplates DEFAULT = new VirtuosoSPARQLTemplates();

    public VirtuosoSPARQLTemplates() {
        add(Ops.UPPER, "bif:upper({0})");
        add(Ops.LOWER, "bif:lower({0})");
        add(Ops.CONCAT, "bif:concat({0},{1})");
        add(Ops.SUBSTR_1ARG, "bif:subseq({0},{1s})");
        add(Ops.SUBSTR_2ARGS, "bif:substring({0},{1s}+1,{2s})");
        add(Ops.TRIM, "bif:trim({0})");
        add(Ops.EQ_IGNORE_CASE, "bif:lower({0}) = bif:lower({1})");
        add(Ops.STRING_IS_EMPTY, "{0} like ''");
        add(Ops.LIKE, "{0} like {1}");
        add(Ops.STRING_LENGTH, "bif:length({0})");

        add(Ops.INDEX_OF, "bif:locate({1},{0})-1");
        add(Ops.INDEX_OF_2ARGS, "bif:locate({1},{0},{2s}+1)-1");

        add(Ops.STARTS_WITH, "bif:locate({1},{0}) = 1");
        add(Ops.ENDS_WITH, "regex({0}, bif:concat({1},'$'))");
        add(Ops.STRING_CONTAINS, "bif:locate({1},{0}) > 0");

        add(Ops.STARTS_WITH_IC, "bif:locate({1l},{0l}) = 1");
        add(Ops.ENDS_WITH_IC, "regex({0}, bif:concat({1},'$'), 'i')");
        add(Ops.STRING_CONTAINS_IC, "bif:locate({1l},{0l}) > 0");

        add(Ops.CHAR_AT, "bif:substring({0},{1s}+1,1)");

        add(Ops.EXISTS, "bif:exists ((select * where {0}))");
        add(Ops.COALESCE, "bif:coalesce({0})");

        // numeric
        add(Ops.MOD, "bif:mod({0},{1})");
        add(Ops.MathOps.ABS, "bif:abs(xsd:double({0}))");
        add(Ops.MathOps.SQRT, "bif:sqrt({0})");

        // date and time
        add(Ops.DateTimeOps.SECOND, "bif:second({0})");
        add(Ops.DateTimeOps.MILLISECOND, "0");
        add(Ops.DateTimeOps.MINUTE, "bif:minute({0})");
        add(Ops.DateTimeOps.HOUR, "bif:hour({0})");
        add(Ops.DateTimeOps.WEEK, "bif:week({0})");
        add(Ops.DateTimeOps.MONTH, "bif:month({0})");
        add(Ops.DateTimeOps.YEAR, "bif:year({0})");
        add(Ops.DateTimeOps.YEAR_MONTH, "bif:year({0}) * 100 + bif:month({0})");
        add(Ops.DateTimeOps.DAY_OF_WEEK, "bif:dayofweek({0})");
        add(Ops.DateTimeOps.DAY_OF_MONTH, "bif:dayofmonth({0})");
        add(Ops.DateTimeOps.DAY_OF_YEAR, "bif:dayofyear({0})");

    }

}
