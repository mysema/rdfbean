/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
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
    public static void setUpClass() throws IOException{
        if (dataSource == null){
            dataSource = JdbcConnectionPool.create("jdbc:h2:nioMapped:target/h2", "sa", "");   
            dataSource.setMaxConnections(30);
        }
        Configuration configuration = new DefaultConfiguration(Entity1.class, Entity2.class, Entity3.class, SimpleType.class, SimpleType2.class);
        repository = new RDBRepository(configuration, dataSource, templates, new MemoryIdSequence());
        repository.initialize();
        
        // enums
        Set<STMT> added = new HashSet<STMT>();
        for (NoteType nt : NoteType.values()){
            added.add(new STMT(
                    new UID(TEST.NS, nt.name()), 
                    CORE.enumOrdinal, 
                    new LIT(String.valueOf(nt.ordinal()), XSD.integerType)));
        }
        RDFConnection connection = repository.openConnection();
        connection.update(Collections.<STMT>emptySet(), added);
        connection.close();
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException, SQLException{
        if (repository != null){
            repository.close();    
        }        
    }

}
