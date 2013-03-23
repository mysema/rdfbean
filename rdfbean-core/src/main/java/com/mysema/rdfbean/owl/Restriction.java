/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.ArrayList;
import java.util.List;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;

/**
 * @author sasa
 * @see http://www.w3.org/TR/2008/WD-owl2-quick-reference-20081202/
 */
@ClassMapping(ns = OWL.NS)
public class Restriction extends OWLClass {

    @Predicate
    private RDFSClass<?> allValuesFrom;

    @Predicate
    private Integer cardinality;

    @Predicate
    private Object hasValue;

    @Predicate
    private Integer maxCardinality;

    @Predicate
    private Integer minCardinality;

    /**
     * OWL 2
     */
    @Predicate
    private List<RDFProperty> onProperties = new ArrayList<RDFProperty>();

    @Predicate
    private RDFProperty onProperty;

    @Predicate
    private RDFSClass<?> someValuesFrom;

    public Restriction() {
        super();
    }

    public RDFSClass<?> getAllValuesFrom() {
        return allValuesFrom;
    }

    public Integer getCardinality() {
        return cardinality;
    }

    public Object getHasValue() {
        return hasValue;
    }

    public Integer getMaxCardinality() {
        return maxCardinality;
    }

    public Integer getMinCardinality() {
        return minCardinality;
    }

    public List<RDFProperty> getOnProperties() {
        return onProperties;
    }

    public RDFProperty getOnProperty() {
        return onProperty;
    }

    public RDFSClass<?> getSomeValuesFrom() {
        return someValuesFrom;
    }

    public void setAllValuesFrom(RDFSClass<?> allValuesFrom) {
        this.allValuesFrom = allValuesFrom;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    public void setHasValue(Object hasValue) {
        this.hasValue = hasValue;
    }

    public void setMaxCardinality(Integer maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setMinCardinality(Integer minCardinality) {
        this.minCardinality = minCardinality;
    }

    public void setOnProperty(RDFProperty onProperty) {
        this.onProperty = onProperty;
    }

    public void setSomeValuesFrom(RDFSClass<?> someValuesFrom) {
        this.someValuesFrom = someValuesFrom;
    }

    public boolean isDefined() {
        return onProperty != null
                && (allValuesFrom != null || cardinality != null
                        || hasValue != null || maxCardinality != null
                        || minCardinality != null || someValuesFrom != null);
    }
}
