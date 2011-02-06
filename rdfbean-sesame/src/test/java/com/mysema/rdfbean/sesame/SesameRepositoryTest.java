package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Test;

import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.io.RDFSource;

public class SesameRepositoryTest {

    private SesameRepository repository;

    @After
    public void tearDown(){
        repository.close();
    }

    @Test(expected=RepositoryException.class)
    public void initialize(){
        repository = new MemoryRepository();
        repository.setSources(new RDFSource("", Format.RDFXML, ""){
            @Override
            public InputStream openStream() throws IOException {
                throw new IllegalStateException();
            }
        });
        repository.initialize();
    }
}
