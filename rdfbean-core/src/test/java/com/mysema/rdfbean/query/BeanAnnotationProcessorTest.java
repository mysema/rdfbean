package com.mysema.rdfbean.query;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

public class BeanAnnotationProcessorTest extends AbstractProcessorTest {

    @Test
    public void Process() throws IOException {
        File file = new File("src/test/java/com/mysema/rdfbean/domains/ContextDomain.java");
        assertTrue(file.exists());
        process(BeanAnnotationProcessor.class, Collections.singletonList(file.getPath()), "rdfbean");
    }

}
