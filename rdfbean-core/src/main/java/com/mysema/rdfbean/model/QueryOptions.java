package com.mysema.rdfbean.model;

/**
 * @author tiwe
 *
 */
public final class QueryOptions {
    
    public static final QueryOptions DEFAULT = new QueryOptions(false, false, false);
    
    private final boolean countViaAggregation;
    
    private final boolean preserveStringOps;

    private final boolean addTypeSuffix;

    public QueryOptions(boolean countViaAggregation, boolean preserveStringOps, boolean addTypeSuffix) {
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
