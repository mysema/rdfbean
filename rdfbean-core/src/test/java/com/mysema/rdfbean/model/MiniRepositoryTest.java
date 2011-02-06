package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;


public class MiniRepositoryTest {

    private final MiniRepository repository = new MiniRepository();
    
    @Before
    public void setUp(){
        List<STMT> stmts = new ArrayList<STMT>();
        stmts.add(new STMT(RDF.type, RDF.type, RDF.Property));
        stmts.add(new STMT(RDF.type, RDFS.label, new LIT("type")));
        stmts.add(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));
        stmts.add(new STMT(RDFS.Resource, RDF.type, RDFS.Class));
        stmts.add(new STMT(RDFS.Resource, RDFS.label, new LIT("Resource")));
        repository.add(stmts.toArray(new STMT[stmts.size()]));
    }

    @Test
    public void Export_RDFXML() throws UnsupportedEncodingException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.RDFXML, null, baos);
        String str = new String(baos.toByteArray(),"UTF-8");
        System.out.println(str);
    }
    
    @Test
    public void Export_Turtle() throws UnsupportedEncodingException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, null, baos);
        String str = new String(baos.toByteArray(),"UTF-8");
        System.out.println(str);
    }

    @Test
    public void Remove_by_Subject(){        
        repository.remove(RDF.type, null, null, null);
        assertTrue(exists(RDFS.Resource, null, null, null));
        assertFalse(exists(RDF.type, null, null, null));
        assertFalse(exists(null, null, RDF.Property, null));
    }
    
    @Test
    public void Remove_by_Object(){        
        repository.remove(null, null, RDF.Property, null);
        assertTrue(exists(RDFS.Resource, null, null, null));
        assertTrue(exists(RDF.type, null, null, null));
        assertFalse(exists(null, null, RDF.Property, null));
    }
    
    @Test
    public void Remove_by_Predicate(){        
        repository.remove(null, RDF.type, null, null);
        assertTrue(exists(RDFS.Resource, null, null, null));
        assertTrue(exists(RDF.type, null, null, null));
        assertFalse(exists(null, RDF.type, null, null));
    }
    
    @Test
    public void Remove_all(){        
        repository.remove(null, null, null, null);
        assertFalse(exists(RDFS.Resource, null, null, null));
        assertFalse(exists(RDF.type, null, null, null));
        assertFalse(exists(null, RDF.type, null, null));
    }
    
    private boolean exists(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, @Nullable UID context) {
        CloseableIterator<STMT> stmts = repository.findStatements(subject, predicate, object, context, false);
        boolean rv = stmts.hasNext();
        stmts.close();
        return rv;
    }
}
