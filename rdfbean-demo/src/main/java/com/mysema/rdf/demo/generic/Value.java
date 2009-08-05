package com.mysema.rdf.demo.generic;

import com.mysema.rdfbean.model.LIT;

public interface Value<T> {

    boolean isLiteral();
    
    boolean isReference();
    
    LIT getLiteral();
    
    T getReference();
}
