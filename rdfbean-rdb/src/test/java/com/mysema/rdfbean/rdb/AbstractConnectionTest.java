/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

public abstract class AbstractConnectionTest {

    private static JdbcConnectionPool dataSource;

    private static SQLTemplates templates = new H2Templates();

    private static RDBRepository repository;

    protected RDFConnection connection;

    @BeforeClass
    public static void setUpClass() throws IOException {
        if (dataSource == null) {
            dataSource = JdbcConnectionPool.create("jdbc:h2:nioMapped:target/h2", "sa", "");
            dataSource.setMaxConnections(30);
        }
        Configuration configuration = new DefaultConfiguration(TEST.NS, Entity1.class, Entity2.class, Entity3.class, SimpleType.class, SimpleType2.class);
        repository = new RDBRepository(configuration, dataSource, templates, new MemoryIdSequence());
        repository.initialize();
    }

    @AfterClass
    public static void tearDownClass() throws IOException, SQLException {
        if (repository != null) {
            repository.close();
        }
    }

    @Before
    public void setUp() {
        connection = repository.openConnection();
    }

    @After
    public void tearDown() {
        if (connection != null) {
            connection.close();
        }
    }
}
