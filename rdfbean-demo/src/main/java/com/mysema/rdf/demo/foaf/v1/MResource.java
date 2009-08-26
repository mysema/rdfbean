package com.mysema.rdf.demo.foaf.v1;

import java.util.Collection;

import com.mysema.rdfbean.model.UID;

public interface MResource {

    Collection<MProperty> getProperties();
    
    //Collection<Property<T>> getProperties(UID... properties);
    
    MProperty getProperty(ID id);
}