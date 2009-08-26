package com.mysema.rdf.demo.foaf.v1;


public abstract interface MValue<T> {
    T getValue();
    void setValue(T value);
}
