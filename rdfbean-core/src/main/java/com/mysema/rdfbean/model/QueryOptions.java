package com.mysema.rdfbean.model;

/**
 * @author tiwe
 * 
 */
public enum QueryOptions {

    // TODO : in -> or (Sesame, Core)

    /**
     *
     */
    ALL(true, true),

    /**
     *
     */
    COUNT_VIA_AGGREGATION(true, false),

    /**
     *
     */
    PRESERVE_STRING_OPS(false, true),

    /**
     *
     */
    DEFAULT(false, false);

    private final boolean countViaAggregation, preserveStringOps;

    private QueryOptions(boolean countViaAggregation, boolean preserveStringOps) {
        this.countViaAggregation = countViaAggregation;
        this.preserveStringOps = preserveStringOps;
    }

    public boolean isCountViaAggregation() {
        return countViaAggregation;
    }

    public boolean isPreserveStringOps() {
        return preserveStringOps;
    }

}
