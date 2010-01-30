/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;

import com.mysema.util.JDBCUtil;

/**
 * DerbyIdentityService provides a Derby based implementation of the
 * IdentityService interface
 * 
 * @author tiwe
 * @version $Id$
 */
public class DerbyIdentityService extends JDBCIdentityService {
    
    static{
        System.setProperty("derby.storage.pageCacheSize", "4000"); 
        System.setProperty("derby.storage.pageSize", "8192");
    }
    
    private final MiniConnectionPoolManager poolManager;
    
    private static final String checkCall = "{ call getlid('a','b') }" ;

    public DerbyIdentityService(String databaseName) throws IOException {
        this(databaseName, 50);
    }
    
    private DerbyIdentityService(String databaseName, int maxConnections) throws IOException {
        EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
        dataSource.setDatabaseName (databaseName);
        dataSource.setCreateDatabase ("create");
        poolManager = new MiniConnectionPoolManager(dataSource, maxConnections);
        init();
    }
    
    @Override
    protected Connection getConnection() throws SQLException {
        return poolManager.getConnection();
    }

    @SuppressWarnings("unchecked")
    private void init() throws IOException {
        Connection connection = null;        
        Statement stmt = null;
        try{
            connection = getConnection();
            connection.setAutoCommit(false);
            CallableStatement cs = null;
            try{
                cs = connection.prepareCall(checkCall);
                return;
            }catch(SQLException e){
                // suppress it
            }finally{
                JDBCUtil.safeClose(cs);
            }
            
            stmt = connection.createStatement();            
            List<String> lines = IOUtils.readLines(getClass().getResourceAsStream("derby.sql"));
            lines.add("");
            StringBuffer buffer = new StringBuffer();
            
            for (String line : lines){
                if (StringUtils.isEmpty(line)){
                    stmt.execute(buffer.toString());
                    buffer = new StringBuffer();
                }else{
                    buffer.append(line).append("\n");
                }                
            }
            connection.commit();
            
        }catch(SQLException e){    
            throw new IOException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(null, stmt, connection);
        }        
    }
    
    public void reset() throws IOException{
        Connection connection = null;        
        Statement stmt = null;        
        try{
            connection = getConnection();
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.execute("DROP TABLE bids");
            stmt.execute("DROP TABLE uids");
            
            stmt.execute("DROP INDEX bids_lid");
            stmt.execute("DROP INDEX bids_id");
            stmt.execute("DROP INDEX uids_lid");
            stmt.execute("DROP INDEX uids_id");
            
            stmt.execute("DROP PROCEDURE getid");
            stmt.execute("DROP PROCEDURE getlid");
            stmt.execute("DROP PROCEDURE createlid");
            connection.commit();
        }catch(SQLException e){
            throw new IOException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(null, stmt, connection);
        }
    }


}
