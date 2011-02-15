package com.mysema.rdfbean.model;

/**
 * @author tiwe
 *
 */
public enum QueryOptions {

    /**
     *
     */
    ALL(true, true, true),

    /**
     *
     */
    COUNT_VIA_AGGREGATION(true, false, false),

    /**
     *
     */
    DEFAULT(false, false, false);

    private final boolean countViaAggregation, preserveStringOps, addTypeSuffix;

    private QueryOptions(boolean countViaAggregation, boolean preserveStringOps, boolean addTypeSuffix) {
        this.countViaAggregation = countViaAggregation;
        this.preserveStringOps = preserveStringOps;
        this.addTypeSuffix = addTypeSuffix;
    }

    public boolean isCountViaAggregation() {
        return countViaAggregation;
    }

    public boolean isPreserveStringOps() {
        return preserveStringOps;
    }

    public boolean isAddTypeSuffix() {
        return addTypeSuffix;
    }

}
