package com.mysema.rdfbean.mulgara;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mulgara.config.MulgaraConfig;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;

/**
 * AbstractMulgaraTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractMulgaraTest {
    
    private static MulgaraRepository repository;
    
    protected RDFConnection connection;
    
    protected int count(CloseableIterator<STMT> stmts) throws IOException {
        int rv = 0;
        try{
            while (stmts.hasNext()){
                rv++;
                stmts.next();
            }    
        }finally{
            stmts.close();    
        }
        return rv;
    }
    
    @BeforeClass
    public static void beforeClass(){
        MulgaraConfig config = new MulgaraConfig();
        repository = new MulgaraRepository(URI.create("mulgara:repo1"), new File("target/mulgara"), config);
        repository.setSources(new RDFSource("classpath:/test.ttl", Format.TURTLE, "test:context1"));
        repository.initialize();
    }
    
    @AfterClass
    public static void afterClass(){
        if (repository != null) repository.close();
    }
    
    @Before
    public void setUp(){         
        connection = repository.openConnection();
    }
    
    @Test
    public void tearDown() throws IOException{
        if (connection != null) connection.close();
    }

}
