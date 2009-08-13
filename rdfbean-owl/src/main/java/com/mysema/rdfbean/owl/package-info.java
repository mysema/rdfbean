/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
@MappedClasses({
    OWLClass.class,
    DataRange.class,
    DatatypeProperty.class,
    ObjectProperty.class,
    Ontology.class,
    Restriction.class,
    Thing.class
})
@DefaultAnnotation( { Nonnull.class } )
package com.mysema.rdfbean.owl;
import javax.annotation.Nonnull;

import com.mysema.rdfbean.annotations.MappedClasses;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;

