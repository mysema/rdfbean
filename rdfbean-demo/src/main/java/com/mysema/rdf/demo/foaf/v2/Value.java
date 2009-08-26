package com.mysema.rdf.demo.foaf.v2;


public interface Value<T> extends Node {
    
    public enum Type {LITERAL, COLLECTION, RESOURCE, UID}
    
    T getValue();
    void setValue(T value);
    
    Type getType();
    void setType(Type type);
}
