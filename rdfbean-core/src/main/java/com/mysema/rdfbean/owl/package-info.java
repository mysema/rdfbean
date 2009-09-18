/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
@DefaultAnnotation( { Nonnull.class })
@MappedClasses({
    DataRange.class, 
    DatatypeProperty.class, 
    ObjectProperty.class, 
    ObjectPropertyFeature.class, 
    Ontology.class,
    OWLClass.class, 
    Restriction.class, 
    Thing.class, 
    TypedList.class})
package com.mysema.rdfbean.owl;

import javax.annotation.Nonnull;

import com.mysema.rdfbean.annotations.MappedClasses;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;

