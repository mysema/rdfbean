/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = RDF.NS, ln = "Property")
public class RDFProperty extends RDFSResource {

    /**
     * FunctionalProperty or AnnotationProperty
     */
    @Predicate(ns = RDF.NS, ln = "type", ignoreInvalid = true)
    private Set<RDFPropertyFeature> basicPropertyFeatures = EnumSet
            .noneOf(RDFPropertyFeature.class);

    @Predicate(ns = RDFS.NS)
    private Set<RDFSClass<?>> domain = new LinkedHashSet<RDFSClass<?>>();

    @Predicate(ns = RDFS.NS)
    private Set<RDFSClass<?>> range = new LinkedHashSet<RDFSClass<?>>();

    @Predicate(ns = RDFS.NS, ln = "subPropertyOf", inv = true)
    private Set<RDFProperty> subProperties = new LinkedHashSet<RDFProperty>();

    @Predicate(ns = RDFS.NS)
    private Set<RDFProperty> subPropertyOf = new LinkedHashSet<RDFProperty>();

    public RDFProperty() {
        super();
    }

    public RDFProperty(UID id) {
        super(id);
    }

    public Set<RDFPropertyFeature> getBasicPropertyFeatures() {
        return basicPropertyFeatures;
    }

    public Set<RDFSClass<?>> getDomain() {
        return domain;
    }

    public Set<RDFSClass<?>> getRange() {
        return range;
    }

    public Set<RDFProperty> getSubProperties() {
        return subProperties;
    }

    public Set<RDFProperty> getSubPropertyOf() {
        return subPropertyOf;
    }

    public boolean isAnnotationProperty() {
        return basicPropertyFeatures
                .contains(RDFPropertyFeature.AnnotationProperty);
    }

    public boolean isFunctionalProperty() {
        return basicPropertyFeatures
                .contains(RDFPropertyFeature.FunctionalProperty);
    }

    public void addRange(RDFSClass<?> range) {
        this.range.add(range);
    }
}
