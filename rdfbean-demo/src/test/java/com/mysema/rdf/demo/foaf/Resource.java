package com.mysema.rdf.demo.foaf;

import java.util.Collection;

import com.mysema.rdfbean.model.UID;

public interface Resource<T> {

    Collection<Property<T>> getProperties();

    // Collection<Property<T>> getProperties(UID... properties);

    Property<T> getProperty(UID uid);
}