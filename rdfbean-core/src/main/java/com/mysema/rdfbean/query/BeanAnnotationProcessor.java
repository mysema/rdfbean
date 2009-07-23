/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import com.mysema.query.annotations.Projection;
import com.mysema.query.apt.Processor;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Inject;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * BeanAnnotationProcessor provides
 *
 * @author tiwe
 * @version $Id$
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BeanAnnotationProcessor extends AbstractProcessor{
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running " + getClass().getSimpleName());
        Class<? extends Annotation> entity = ClassMapping.class;
        Class<? extends Annotation> superType = null; // undefined
        Class<? extends Annotation> embeddable = null; // undefined
        Class<? extends Annotation> dtoAnnotation = Projection.class;
        Processor p = new Processor(processingEnv, entity, superType, embeddable, dtoAnnotation, "Q"){

            @Override
            protected boolean isValidField(VariableElement field) {
                return super.isValidField(field) && isValid(field);
            }

            @Override
            protected boolean isValidGetter(ExecutableElement getter){
                return super.isValidGetter(getter) && isValid(getter);
            }
            
            private boolean isValid(Element d){
                return d.getAnnotation(Inject.class) == null && 
                    (d.getAnnotation(Predicate.class) != null 
                    || d.getAnnotation(Mixin.class) != null 
                    || d.getAnnotation(Id.class) != null);
            }
            
        };
        p.process(roundEnv);
        return true;
    }       
    
}