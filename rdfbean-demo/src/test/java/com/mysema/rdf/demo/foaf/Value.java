package com.mysema.rdf.demo.foaf;

import com.mysema.rdfbean.model.LIT;

public interface Value<T> {

    boolean isLiteral();

    boolean isReference();

    LIT getLiteral();

    T getReference();
}
