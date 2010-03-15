/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FieldIdSourceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FieldIdSourceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldIdSourceTest.class);
    
    private File file;
    
    private FileIdSequence idSource;
    
    @Before
    public void setUp(){
        file = new File("target", String.valueOf(System.currentTimeMillis()));
        if (file.exists()){
            if (!file.delete()){
                logger.error("Deletion of " + file.getPath() + " failed");
            }
        }        
        idSource = new FileIdSequence(file, 100);
    }
    
    @After
    public void tearDown() throws IOException{        
        idSource.close();
        if (file.exists()){
            if (!file.delete()){
                logger.error("Deletion of " + file.getPath() + " failed");
            }
        }
    }
    
    @Test
    public void test() throws IOException{
        // 001-100
        // 101-200
        // 201-300
        long s = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++){
            idSource.getNextId();
        }
        long e = System.currentTimeMillis();
        System.out.println((e-s)+"ms");
        idSource.close();        
        idSource = new FileIdSequence(file, 100);
        assertEquals(10001l, idSource.getNextId());
    }
    
    @Test
    public void test2() throws IOException{
        for (int i = 0; i < 50; i++){
            idSource.getNextId();
        }
        idSource.close();        
        idSource = new FileIdSequence(file, 100);
        assertEquals(101l, idSource.getNextId());
    }
    
    @Test
    public void test3() throws IOException{
        for (int i = 0; i < 300; i++){
            idSource.getNextId();
        }        
    }

}
