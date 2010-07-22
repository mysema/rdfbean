/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;

/**
 * MD5Test provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MD5IdFactoryTest {
    
    private IdFactory idFactory = new MD5IdFactory();
    
    @Test
    public void testNode() throws UnsupportedEncodingException, NoSuchAlgorithmException{        
        Set<Long> seen = new HashSet<Long>();        
        Set<NODE> nodes = new HashSet<NODE>();
        // UID
        nodes.addAll(RDF.ALL);
        nodes.addAll(RDFS.ALL);
        nodes.addAll(XSD.ALL);
        nodes.addAll(OWL.ALL);
        // BID
        nodes.add(new BID(RDF.type.getValue()));
        nodes.add(new BID("1"));
        // LIT
        nodes.add(new LIT(RDF.type.getValue()));
        nodes.add(new LIT("1",Locale.ENGLISH));
        nodes.add(new LIT("1",XSD.stringType));
        nodes.add(new LIT("1",XSD.integerType));
        nodes.add(new LIT("true", XSD.booleanType));
        nodes.add(new LIT("false", XSD.booleanType));
        
        nodes.add(new LIT("1"+Locale.ENGLISH));
        nodes.add(new LIT("1"+XSD.stringType));
        
        for (int i = -128; i < 128; i++){
            String str = String.valueOf(i);
            nodes.add(new LIT(str, XSD.byteType));
            nodes.add(new LIT(str, XSD.intType));
            nodes.add(new LIT(str, XSD.longType));
            nodes.add(new LIT(str+".0", XSD.doubleType));
            nodes.add(new LIT(str+".0", XSD.floatType));
        }
        
        for (int i = 128; i < 10000; i++){
            String str = String.valueOf(i);
            nodes.add(new LIT(str, XSD.intType));
            nodes.add(new LIT("-"+str, XSD.intType));
            nodes.add(new LIT(str, XSD.longType));
            nodes.add(new LIT("-"+str, XSD.longType));
            nodes.add(new LIT(str+".0", XSD.doubleType));
            nodes.add(new LIT("-"+str+".0", XSD.doubleType));
            nodes.add(new LIT(str+".0", XSD.floatType));
            nodes.add(new LIT("-"+str+".0", XSD.floatType));
        }
        
        for (NODE node : nodes){
            Long id = idFactory.getId(node);
            if (seen.contains(id)){
                throw new IllegalStateException(id + " already used");
            }            
            seen.add(id);
        }    
    }
    
    @Test
    public void testLang(){
        Set<Integer> seen = new HashSet<Integer>();        
        for (Locale locale : Locale.getAvailableLocales()){
            Integer id = idFactory.getId(locale);
            if (seen.contains(id)){
                throw new IllegalStateException(id + " already used");
            }
            seen.add(id);
        }
    }
    
    

}
