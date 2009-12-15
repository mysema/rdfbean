/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.compass.core.config.CompassConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.owl.OWL;


/**
 * NodeConverterTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NodeConverterTest {
    
    private NodeConverter converter;
    
    @Before
    public void setUp(){
        DefaultLuceneConfiguration configuration = new DefaultLuceneConfiguration();        
        configuration.setCompassConfig(new CompassConfiguration());
        configuration.setCoreConfiguration(new DefaultConfiguration());
        configuration.setOnline(false);
        configuration.initialize();
        converter = configuration.getConverter();
    }
    
    @Test
    public void test(){
        List<NODE> nodes = Arrays.<NODE>asList(
                new LIT("lit"),
                new LIT("lit", "fi"),
                new LIT("lit", "f.i"),
                new LIT("1.0", XSD.doubleType),
                new LIT("lit", XSD.stringType),
                new LIT("lit", new UID("http://www.test.com")),
                new UID("http://www.test.com"),
                XSD.stringType,
                RDF.Property,
                RDFS.comment,
                OWL.allValuesFrom,
                new BID("nodeA"),
                new BID()
        );
        
        for (NODE node : nodes){
            String str = converter.toString(node);            
            System.out.println(str);
            assertEquals(node, converter.fromString(str));
        }
    }
    
    @Test
    public void testStringLiteral(){
        assertEquals("Test|l", converter.toString(new LIT("Test")));
        assertEquals("Test|l", converter.toString(new LIT("Test", XSD.stringType)));
    }

}
