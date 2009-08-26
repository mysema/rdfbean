package com.mysema.rdf.demo.foaf.v2;

import java.util.Collection;

public interface TypedCollection extends Value<Collection<Value<?>>>{

    public enum Type {BAG, SEQ, ALT}
    
    Type getCollectionType();
    void setCollectionType(Type type);
}
