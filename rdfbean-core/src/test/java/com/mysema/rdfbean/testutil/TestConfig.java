package com.mysema.rdfbean.testutil;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Ignore;

@Ignore
@Documented
@Target( TYPE )
@Retention(RetentionPolicy.RUNTIME)
public @interface TestConfig {

    Class<?>[] value();
    
}
