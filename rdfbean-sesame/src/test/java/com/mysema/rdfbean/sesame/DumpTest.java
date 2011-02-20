package com.mysema.rdfbean.sesame;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.mysema.rdfbean.model.Format;

public class DumpTest {

    public static void main(String[] args) throws UnsupportedEncodingException{
        MemoryRepository repository = new MemoryRepository();
        repository.initialize();        
        repository.load(Format.TURTLE, DumpTest.class.getResourceAsStream("/test.ttl"), null, false);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.export(Format.NTRIPLES, null, out);
        System.err.println(new String(out.toByteArray(), "UTF-8"));
    }
    
}
