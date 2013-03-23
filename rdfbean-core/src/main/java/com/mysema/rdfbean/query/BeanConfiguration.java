/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Collections;
import java.util.Map;

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
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * @author tiwe
 */
public class BeanConfiguration extends DefaultConfiguration {

    public BeanConfiguration(RoundEnvironment roundEnv, Map<String, String> options) {
        super(roundEnv,
                options,
                Collections.<String> emptySet(), // keywords
                null, // entities
                ClassMapping.class,
                null, // super
                null, // embeddable
                null, // embedded
                QueryTransient.class);
    }

    @Override
    public boolean isValidField(VariableElement field) {
        return super.isValidField(field) && isValid(field);
    }

    @Override
    public boolean isValidGetter(ExecutableElement getter) {
        return super.isValidGetter(getter) && isValid(getter);
    }

    private boolean isValid(Element d) {
        return d.getAnnotation(InjectService.class) == null &&
                (d.getAnnotation(Predicate.class) != null
                        || d.getAnnotation(Path.class) != null
                        || d.getAnnotation(Mixin.class) != null
                        || d.getAnnotation(Id.class) != null);
    }

}
