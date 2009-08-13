/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
@MappedClasses({
    RDFProperty.class,
    RDFPropertyFeature.class,
    RDFSClass.class,
    RDFSDatatype.class,
    RDFSResource.class
})
@DefaultAnnotation( { Nonnull.class } )
package com.mysema.rdfbean.rdfs;
import javax.annotation.Nonnull;

import com.mysema.rdfbean.annotations.MappedClasses;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;

