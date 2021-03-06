/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

/**
 * Declares the inferencing options that are not provided by the RDF persistence
 * engine and are to be supported on RDFBean level
 * 
 * @author tiwe
 */
public enum InferenceOptions {

    /**
     *
     */
    CLASS(true, false, false),

    /**
     *
     */
    CLASS_LITERAL(true, false, true),

    /**
     *
     */
    CLASS_PROPERTY(true, true, false),

    /**
     *
     */
    CLASS_PROPERTY_LIST(true, true, true),

    /**
     *
     */
    DEFAULT(true, true, false),

    /**
     *
     */
    PROPERTY(false, true, false),

    /**
     *
     */
    NONE(false, false, false),

    /**
     *
     */
    PROPERTY_LITERAL(false, true, true),

    /**
     *
     */
    LITERAL(false, false, true);

    private final boolean subClassOf, subPropertyOf, untypedAsString;

    private InferenceOptions(boolean subClassOf, boolean subPropertyOf, boolean untypedAsString) {
        this.subClassOf = subClassOf;
        this.subPropertyOf = subPropertyOf;
        this.untypedAsString = untypedAsString;
    }

    /**
     * Support for matching subtype instances
     * 
     * @return
     */
    public boolean subClassOf() {
        return subClassOf;
    }

    /**
     * Support for matching subproperty patterns
     * 
     * @return
     */
    public boolean subPropertyOf() {
        return subPropertyOf;
    }

    /**
     * Support for matching untyped string as xsd:string typed
     * 
     * @return
     */
    public boolean untypedAsString() {
        return untypedAsString;
    }
}
