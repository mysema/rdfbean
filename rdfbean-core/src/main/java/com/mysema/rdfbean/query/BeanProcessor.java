/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import com.mysema.query.annotations.DTO;
import com.mysema.query.annotations.Entity;
import com.mysema.query.apt.general.DefaultEntityVisitor;
import com.mysema.query.apt.general.GeneralProcessor;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Inject;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;

/**
 * BeanProcessor extends the GeneralProcessor from querydsl-apt to provide RDFBean specific processing
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanProcessor extends GeneralProcessor{

    public BeanProcessor(AnnotationProcessorEnvironment env) {
        super(env, Entity.class.getName(), ClassMapping.class.getName(), DTO.class.getName());
    }
    
    @Override
    protected DefaultEntityVisitor createEntityVisitor(){
        return new DefaultEntityVisitor(){
            @Override
            public void visitFieldDeclaration(FieldDeclaration d) {      
                if (isIDField(d)){
                    super.visitFieldDeclaration(d);
                }else if (isNormalField(d)){
                    super.visitFieldDeclaration(d);
                }
            }
            @Override
            public void visitMethodDeclaration(MethodDeclaration d) {
                if (isIDField(d)){
                    super.visitMethodDeclaration(d);
                }else if (isNormalField(d)){
                    super.visitMethodDeclaration(d);
                }
            }
        };
    }
    
    private boolean isIDField(MemberDeclaration d){
        return d.getAnnotation(Id.class) != null;
    }
    
    private boolean isNormalField(MemberDeclaration d){        
        return d.getAnnotation(Inject.class) == null && 
            (d.getAnnotation(Predicate.class) != null || d.getAnnotation(Mixin.class) != null);
    }
}
