package com.mysema.rdfbean.model;

import java.io.File;

import org.junit.Test;

/**
 * FieldIdSourceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FieldIdSourceTest {
    
    @Test
    public void test(){
        File file = new File("target", String.valueOf(System.currentTimeMillis()));
        FileIdSource idSource = new FileIdSource(file); 
        long s = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++){
            idSource.getNextId();
        }
        long e = System.currentTimeMillis();
        System.out.println((e-s)+"ms");
    }

}
