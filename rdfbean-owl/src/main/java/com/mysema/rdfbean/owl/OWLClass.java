/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSResource;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=OWL.NS, ln="Class")
public class OWLClass extends RDFSClass<RDFSResource> {

    @Predicate
    private Set<OWLClass> complementOf = new LinkedHashSet<OWLClass>();

	@Predicate
    private Set<OWLClass> disjointWith = new LinkedHashSet<OWLClass>();

	@Predicate
    private List<OWLClass> intersectionOf = new ArrayList<OWLClass>();

    @Predicate
    private List<OWLClass> unionOf = new ArrayList<OWLClass>();

    public OWLClass() {
		super();
	}

    public OWLClass(ID id) {
		super(id);
	}

    public Set<OWLClass> getComplementOf() {
        return complementOf;
    }

    public Set<OWLClass> getDisjointWith() {
        return disjointWith;
    }

    public List<OWLClass> getIntersectionOf() {
        return intersectionOf;
    }

    public List<OWLClass> getUnionOf() {
        return unionOf;
    }
    
    public void setAllValuesFrom(RDFProperty property, RDFSClass<?> allValuesFrom) {
        Restriction restriction = new Restriction();
        restriction.setOnProperty(property);
        restriction.setAllValuesFrom(allValuesFrom);
        addSuperClass(restriction);
    }
    
}
