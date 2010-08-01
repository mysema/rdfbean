/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Mixin;

/**
 * @author tiwe
 *
 */
@SuppressWarnings("all")
public class MixinImpl implements Mixin {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Mixin.class;
    }

}
