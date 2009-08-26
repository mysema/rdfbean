package com.mysema.rdf.demo.foaf.v1;

public interface MReference<T> extends MValue<T> {
    ID getId();
    Class<?> getType();
}