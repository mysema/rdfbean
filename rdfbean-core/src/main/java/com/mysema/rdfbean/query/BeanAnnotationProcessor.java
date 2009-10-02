/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.Nullable;
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
import com.mysema.query.annotations.Transient;
import com.mysema.query.apt.Configuration;
import com.mysema.query.apt.Processor;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.InjectService;
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
    
    private Class<? extends Annotation> entity, dto, skip;
    
    @Nullable 
    private Class<? extends Annotation> superType, embeddable;
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running " + getClass().getSimpleName());
        entity = ClassMapping.class;
        superType = null; // undefined
        embeddable = null; // undefined
        dto = Projection.class;
        skip = Transient.class;
        
        Configuration configuration = new Configuration(entity, superType, embeddable, dto, skip){
            @Override
            public boolean isValidField(VariableElement field) {
                return super.isValidField(field) && isValid(field);
            }

            @Override
            public boolean isValidGetter(ExecutableElement getter){
                return super.isValidGetter(getter) && isValid(getter);
            }
            
            private boolean isValid(Element d){
                return d.getAnnotation(InjectService.class) == null && 
                    (d.getAnnotation(Predicate.class) != null 
                    || d.getAnnotation(Mixin.class) != null 
                    || d.getAnnotation(Id.class) != null);
            }
        };
        
        Processor p = new Processor(processingEnv, configuration);
        p.process(roundEnv);
        return true;
    }       
    
}