package com.mysema.rdf.demo.generic;

import com.mysema.rdfbean.model.UID;

public interface PropertyAccess<RefType> extends Iterable<RefType> {

    UID getUID();

    boolean isSingleValue();

    boolean isLocalized();
    
    boolean isLiteral();
    
    boolean isReference();
    
    boolean istList();
    
    boolean isContainer();

}
