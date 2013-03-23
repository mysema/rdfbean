package com.mysema.rdfbean.rdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

@Ignore
public class DBPediaLoadTest {

    private RDBRepository repository;

    // private String path = "../../geocoordinates-fixed.nt";
    private String path = "../../homepages-fixed-subset.nt";

    @Before
    public void setUp() {
        new File("target/dbpedia.h2.db").delete();
        new File("target/dbpedia.trace.db").delete();
        JdbcConnectionPool dataSource = JdbcConnectionPool.create("jdbc:h2:nio:target/dbpedia", "sa", "");
        dataSource.setMaxConnections(30);
        SQLTemplates templates = new H2Templates();

        // MysqlConnectionPoolDataSource dataSource = new
        // MysqlConnectionPoolDataSource();
        // dataSource.setUrl("jdbc:mysql://localhost:3306/rdfbean");
        // dataSource.setUser("rdfbean");
        // dataSource.setPassword("rdfbean");
        // SQLTemplates templates = new MySQLTemplates();

        Configuration configuration = new DefaultConfiguration();
        repository = new RDBRepository(configuration, dataSource, templates, new MemoryIdSequence());
        repository.initialize();
    }

    @After
    public void tearDown() {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void Load_Into_Repository() throws FileNotFoundException {
        InputStream is = new FileInputStream(path);
        long start = System.currentTimeMillis();
        repository.load(Format.NTRIPLES, is, null, false);
        System.out.println(System.currentTimeMillis() - start);
        // 12.469
        // 9.314
        // 8.067
    }

}
