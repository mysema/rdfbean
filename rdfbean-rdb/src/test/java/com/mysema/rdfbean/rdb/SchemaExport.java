/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.MetaDataExporter;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * SchemaExport provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SchemaExport {
    
    public static void main(String[] args) throws SQLException{
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:target/h2");
        ds.setUser("sa");
        ds.setPassword("");
        Connection conn = ds.getConnection();
        
        // export
        try{
            Configuration configuration = new DefaultConfiguration();
            SQLTemplates templates = new H2Templates();
            Repository repository = new RDBRepository(configuration, ds, templates, new MemoryIdSequence());
            repository.initialize();
            
            MetaDataExporter exporter = new MetaDataExporter("Q", "com.mysema.rdfbean.rdb.schema", null, null, new File("src/main/java"));
            exporter.export(conn.getMetaData());    
        }finally{
            conn.close();
        }
        
        
    }

}
