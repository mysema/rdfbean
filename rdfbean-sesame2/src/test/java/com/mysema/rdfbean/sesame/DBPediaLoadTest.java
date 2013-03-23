package com.mysema.rdfbean.sesame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.Format;

@Ignore
public class DBPediaLoadTest {

    private NativeRepository repository;

    // private String path = "../../geocoordinates-fixed.nt";
    private String path = "../../homepages-fixed-subset.nt";

    @After
    public void tearDown() {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void test() throws FileNotFoundException {
        repository = new NativeRepository();
        repository.setDataDir(new File("target/DBPedia"));
        repository.initialize();
        InputStream is = new FileInputStream(path);
        long start = System.currentTimeMillis();
        repository.load(Format.NTRIPLES, is, null, false);
        System.out.println(System.currentTimeMillis() - start);
        // 3.536
    }

}
