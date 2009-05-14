/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mysema.query.annotations.DTO;
import com.mysema.query.annotations.Entity;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * BeanAPTFactory is an AnnotationProcessingFactory implementation which creates BeanProcessor 
 * instances to process the following annotation types
 * 
 * <ul>
 *   <li>Entity</li>
 *   <li>ClassMapping</li>
 *   <li>DTO</li>
 * </ul>
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanAPTFactory implements AnnotationProcessorFactory{
    
    private static final Collection<String> supportedAnnotations = Arrays.asList(Entity.class.getName(),ClassMapping.class.getName(), DTO.class.getName());

    private static final Collection<String> supportedOptions = Collections.emptySet();

    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }

    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, 
            AnnotationProcessorEnvironment env) {
        return new BeanProcessor(env);
    }

}
