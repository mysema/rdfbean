package com.mysema.rdfbean.model;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.io.Format;


public class MiniRepositoryTest {
    
    private MiniRepository repository = new MiniRepository();
    
    @Test
    @Ignore
    public void Export() throws UnsupportedEncodingException{
        List<STMT> stmts = new ArrayList<STMT>();
        stmts.add(new STMT(RDF.type, RDF.type, RDF.Property));
        stmts.add(new STMT(RDF.type, RDFS.label, new LIT("type")));
        stmts.add(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));        
        stmts.add(new STMT(RDFS.Resource, RDF.type, RDFS.Class));
        stmts.add(new STMT(RDFS.Resource, RDFS.label, new LIT("Resource")));
        repository.add(stmts.toArray(new STMT[stmts.size()]));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, baos);
        String str = new String(baos.toByteArray(),"UTF-8");
        System.out.println(str);
        
    }
    

}
