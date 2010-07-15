package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.testutil.SessionRule;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRDBTest {
    
    protected static JdbcDataSource dataSource;
    
    protected static SQLTemplates templates = new H2Templates();
    
    protected static RDBRepository repository;
    
    @Rule
    public SessionRule sessionRule = new SessionRule(repository);
    
    public Session session;
    
    @BeforeClass
    public static void setUpClass(){
//        new File("target/h2.h2.db").delete();
//        new File("target/h2.trace.db").delete();        
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:target/h2");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        
        Configuration configuration = new DefaultConfiguration();
        
        repository = new RDBRepository(configuration, dataSource, templates, new MemoryIdSequence());
        repository.initialize();
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, SQLException{
        if (repository != null){
            repository.close();    
        }        
    }

}
