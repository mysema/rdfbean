/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.annotations;

/**
 * Whether or not the value is stored in the document
 * 
 * @author Emmanuel Bernard
 */
public enum Store {
    /** does not store the value in the index */
    NO,
    /** stores the value in the index */
    YES,
    /** stores the value in the index in a compressed form */
    COMPRESS
}
