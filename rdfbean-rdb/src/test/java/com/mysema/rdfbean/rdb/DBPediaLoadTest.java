package com.mysema.rdfbean.rdb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.io.Format;

public class DBPediaLoadTest extends AbstractRDBTest{

    @Test
    @Ignore
    public void test() throws FileNotFoundException{
        InputStream is = new FileInputStream("../../geocoordinates-fixed.nt");
        long start = System.currentTimeMillis();
        repository.load(Format.NTRIPLES, is, null, false);
        System.out.println(System.currentTimeMillis()-start);
        // 620506ms = 10m
    }
}
