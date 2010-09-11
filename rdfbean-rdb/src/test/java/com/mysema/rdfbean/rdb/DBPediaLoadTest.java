package com.mysema.rdfbean.rdb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.io.Format;

@Ignore
public class DBPediaLoadTest extends AbstractRDBTest{

    @Test    
    public void geocoordinates() throws FileNotFoundException{
        InputStream is = new FileInputStream("../../geocoordinates-fixed.nt");
        long start = System.currentTimeMillis();
        repository.load(Format.NTRIPLES, is, null, false);
        System.out.println(System.currentTimeMillis()-start);
        // 620506ms = 10m
        // 293801

    }
    
    @Test    
    public void homepages() throws FileNotFoundException{
        InputStream is = new FileInputStream("../../homepages-fixed.nt");
        long start = System.currentTimeMillis();
        repository.load(Format.NTRIPLES, is, null, false);
        System.out.println(System.currentTimeMillis()-start);
    }
}
