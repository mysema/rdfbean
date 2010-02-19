package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * FieldIdSourceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FieldIdSourceTest {
    
    private File file;
    
    private FileIdSource idSource;
    
    @Before
    public void setUp(){
        file = new File("target", String.valueOf(System.currentTimeMillis()));
        file.delete();
        idSource = new FileIdSource(file, 100);
    }
    
    @After
    public void tearDown() throws IOException{
        idSource.close();
    }
    
    @Test
    public void test() throws IOException{
        long s = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++){
            idSource.getNextId();
        }
        long e = System.currentTimeMillis();
        System.out.println((e-s)+"ms");
        idSource.close();        
        idSource = new FileIdSource(file, 100);
        assertEquals(10101l, idSource.getNextId());
    }
    
    @Test
    public void test2() throws IOException{
        for (int i = 0; i < 50; i++){
            idSource.getNextId();
        }
        idSource.close();        
        idSource = new FileIdSource(file, 100);
        assertEquals(101l, idSource.getNextId());
    }

}
