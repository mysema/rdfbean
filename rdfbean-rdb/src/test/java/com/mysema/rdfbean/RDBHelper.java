package com.mysema.rdfbean;

import org.h2.jdbcx.JdbcConnectionPool;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.rdb.RDBRepository;

public class RDBHelper extends Helper{
    
    private static JdbcConnectionPool dataSource;

    private static SQLTemplates templates = new H2Templates();

    @Override
    public Repository createRepository() {
        if (dataSource == null){
            dataSource = JdbcConnectionPool.create("jdbc:h2:nioMapped:target/h2", "sa", "");
            dataSource.setMaxConnections(30);
        }
        Configuration configuration = new DefaultConfiguration(TEST.NS, Entity1.class, Entity2.class, Entity3.class, SimpleType.class, SimpleType2.class);
        return new RDBRepository(configuration, dataSource, templates, new MemoryIdSequence());
    }

}
