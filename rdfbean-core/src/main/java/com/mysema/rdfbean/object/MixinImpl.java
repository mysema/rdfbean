package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Mixin;

/**
 * @author tiwe
 *
 */
public class MixinImpl implements Mixin {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Mixin.class;
    }

}
