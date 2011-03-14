package com.mysema.rdfbean.maven;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class JavaBeanGenMojoTest {

    @Test
    public void Execute() throws SecurityException, NoSuchFieldException, IllegalAccessException, MojoExecutionException, MojoFailureException {
        JavaBeanGenMojo mojo = new JavaBeanGenMojo();
        set(mojo, "schemaFile", new File("src/test/resources/wine.owl"));
        set(mojo, "targetFolder", new File("target/JavaBeanGenMojoTest"));
        set(mojo, "nsToPackage", Collections.singletonMap("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#", "com.example.wine"));
        mojo.execute();

        assertTrue(new File("target/JavaBeanGenMojoTest/com/example/wine/Wine.java").exists());
    }

    private void set(Object obj, String fieldName, Object value) throws SecurityException, NoSuchFieldException, IllegalAccessException{
        Field field = JavaBeanGenMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }


}
