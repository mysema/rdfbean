/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.mysema.commons.lang.Assert;
import com.mysema.util.JDBCUtil;

/**
 * DerbyIdentityService provides a Derby based implementation of the
 * IdentityService interface
 * 
 * @author tiwe
 * @version $Id$
 */
public class DerbyIdentityService extends JDBCIdentityService {
    
    private String connectionUrl, username, password;
    
    private final String checkCall = "{ call getlid('a','b',1) }" ;

    public DerbyIdentityService(String connectionUrl) throws IOException {
        this(connectionUrl, "", "");
    }
    
    public DerbyIdentityService(String connectionUrl, String username, String password) throws IOException {
        this.connectionUrl = Assert.hasText(connectionUrl);
        this.username = username;
        this.password = password;        
        init();
    }
    
    @Override
    protected Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(connectionUrl, username, password);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void init() throws IOException {
        Connection connection = getConnection();        
        Statement stmt = null;
        try{
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
        Connection connection = getConnection();
        Statement stmt = null;
        
        try{
            stmt = connection.createStatement();
            stmt.execute("DROP TABLE ids");
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
