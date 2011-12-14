/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import com.mysema.query.apt.AbstractQuerydslProcessor;
import com.mysema.query.apt.Configuration;

/**
 * @author tiwe
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BeanAnnotationProcessor extends AbstractQuerydslProcessor{
    
    @Override
    protected Configuration createConfiguration(RoundEnvironment roundEnv) {
    	return new BeanConfiguration(roundEnv, processingEnv.getOptions());   
    }
    
}