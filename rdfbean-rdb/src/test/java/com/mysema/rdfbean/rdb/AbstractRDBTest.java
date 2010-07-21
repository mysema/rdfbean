/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
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
    
    protected static JdbcConnectionPool dataSource;
    
    protected static SQLTemplates templates = new H2Templates();
    
    protected static RDBRepository repository;
    
    @Rule
    public SessionRule sessionRule = new SessionRule(repository);
    
    public Session session;
    
    @BeforeClass
    public static void setUpClass(){        
        Configuration configuration = new DefaultConfiguration(Entity1.class, Entity2.class, Entity3.class, SimpleType.class, SimpleType2.class);
        dataSource = JdbcConnectionPool.create("jdbc:h2:target/h2", "sa", "");        
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
