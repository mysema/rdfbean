/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.owl.OWL;


/**
 * @author sasa
 *
 */
@ClassMapping(ns=RDFS.NS, ln="Class")
public class RDFSClass<D> extends RDFSResource {

    @Predicate(ns=OWL.NS)
    private List<D> oneOf = new ArrayList<D>();

	@Predicate(ln="domain", inv=true)
    private Set<RDFProperty> properties = new LinkedHashSet<RDFProperty>();

	@Predicate(ln="subClassOf", inv=true)
    private Set<RDFSClass<D>> subClasses = new LinkedHashSet<RDFSClass<D>>();

    @Predicate(ln="subClassOf")
    private Set<RDFSClass<D>> superClasses = new LinkedHashSet<RDFSClass<D>>();
    
    public RDFSClass() {
		super();
	}
    
    public RDFSClass(ID id) {
		super(id);
	}

    public void addSuperClass(RDFSClass<D> superClass) {
    	superClasses.add(superClass);
    }

    public List<D> getOneOf() {
        return oneOf;
    }

    public Set<RDFProperty> getProperties() {
        return properties;
    }

    public Set<RDFSClass<D>> getSubClasses() {
        return subClasses;
    }
    
    public Set<RDFSClass<D>> getSuperClasses() {
        return superClasses;
    }

    public void setOneOf(List<D> oneOf) {
        this.oneOf = oneOf;
    }
}
