package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.sesame.query.QSimpleType;
import com.mysema.rdfbean.sesame.query.SimpleType;
import com.mysema.rdfbean.sesame.query.SimpleType2;

/**
 * PlainLiteralTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PlainLiteralTest{
    
    private final QSimpleType simpleType = QSimpleType.simpleType;
        
    private MemoryRepository repository;
    
    @Before
    public void setUp(){
        repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/test.ttl", RDFFormat.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", RDFFormat.RDFXML, FOAF.NS)
        );
        repository.initialize();    
    }
    
    @Test
    public void test() throws StoreException, IOException{        
        Session session = SessionUtil.openSession(repository, new Locale("fi"), SimpleType.class, SimpleType2.class);
        
        // get instance with plain literal value
        SimpleType instance = session
            .from(simpleType)
            .where(simpleType.directProperty.eq("metaonto_elements"))
            .uniqueResult(simpleType);
        assertNotNull(instance);
        
        // modify value and save it
        instance.setDirectProperty("new value");
        session.save(instance);
        session.flush();
        session.close();
        
        // reload it
        session = SessionUtil.openSession(repository, new Locale("fi"), SimpleType.class, SimpleType2.class);
        instance = session
            .from(simpleType)
            .where(simpleType.directProperty.eq("new value"))
            .uniqueResult(simpleType);
        assertNotNull(instance);
        assertEquals("new value", instance.getDirectProperty());
        
    }

}
