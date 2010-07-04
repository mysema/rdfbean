package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.model.MemoryIdSequence;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRDBTest {
    
    protected static JdbcDataSource dataSource;
    
    protected static SQLTemplates templates = new H2Templates();
    
    protected RDBRepository repository;
    
    @BeforeClass
    public static void setUpClass(){
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:target/h2");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }
    
    @Before
    public void setUp() throws SQLException{
        repository = new RDBRepository(dataSource, templates, new MemoryIdSequence());
        repository.initialize();
    }
    
    @After
    public void tearDown() throws IOException, SQLException{
        if (repository != null){
            repository.close();    
        }        
    }

}
