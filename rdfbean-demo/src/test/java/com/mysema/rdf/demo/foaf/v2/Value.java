package com.mysema.rdf.demo.foaf.v2;


public abstract interface Value<T> {
    T getValue();
    void setValue(T value);
}
