/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * Declares the inferencing options that are to be supported on RDFBean level
 *
 * @author tiwe
 * @version $Id$
 */
public class InferenceOptions {
    
    private final boolean subClassOf, subPropertyOf, untypedAsString;
    
    public InferenceOptions(boolean subClassOf, boolean subPropertyOf, boolean untypedAsString){
        this.subClassOf = subClassOf;
        this.subPropertyOf = subPropertyOf;
        this.untypedAsString = untypedAsString;
    }

    public boolean subClassOf(){
        return subClassOf;
    }

    public boolean subPropertyOf(){
        return subPropertyOf;
    }

    public boolean untypedAsString(){
        return untypedAsString;
    }
}
