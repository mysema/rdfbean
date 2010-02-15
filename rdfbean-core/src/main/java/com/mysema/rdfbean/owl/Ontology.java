/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.LinkedHashSet;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.rdfs.MappedResourceBase;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = OWL.NS)
public class Ontology extends MappedResourceBase {

    @Predicate
    private Set<Ontology> imports = new LinkedHashSet<Ontology>();

    public Ontology() {
        super();
    }

    public Ontology(UID id) {
        super(id);
    }

    public Set<Ontology> getImports() {
        return imports;
    }

    public void addImport(Ontology importedOntology) {
        imports.add(importedOntology);
    }
}
