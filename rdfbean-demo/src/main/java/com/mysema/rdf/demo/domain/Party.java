package com.mysema.rdf.demo.domain;

import com.mysema.rdf.demo.DEMO;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

@ClassMapping(ns = DEMO.NS)
public abstract class Party {

    @Id(IDType.LOCAL)
    private String id;

    public String getId() {
        return id;
    }

    @Predicate
    public abstract String getDisplayName();

}
