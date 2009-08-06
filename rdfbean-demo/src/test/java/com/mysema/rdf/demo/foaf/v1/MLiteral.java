package com.mysema.rdf.demo.foaf.v1;

public interface MLiteral<T> extends MValue<T> {
    Class<?> getType();
}
