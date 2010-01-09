/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import com.mysema.query.annotations.QueryTransient;
import com.mysema.query.apt.DefaultConfiguration;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.InjectService;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * BeanConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanConfiguration extends DefaultConfiguration {
    
    public BeanConfiguration(RoundEnvironment roundEnv) {
        super(roundEnv, ClassMapping.class, null, null, QueryTransient.class);
    }
    
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

}
