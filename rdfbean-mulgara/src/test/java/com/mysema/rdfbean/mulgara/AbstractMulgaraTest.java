package com.mysema.rdfbean.mulgara;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
    public static void beforeClass() throws MarshalException, ValidationException{
        InputStream is = AbstractMulgaraTest.class.getResourceAsStream("/mulgara-config-xa1.xml");
        MulgaraConfig config = MulgaraConfig.unmarshal(new InputStreamReader(is));
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
    
    @After
    public void tearDown() throws IOException{
        if (connection != null) connection.close();
    }

}
