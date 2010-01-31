/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.JDBCUtil;
import com.mysema.util.MiniConnectionPoolManager;

/**
 * DerbyIdentityService provides a Derby based implementation of the
 * IdentityService interface
 * 
 * @author tiwe
 * @version $Id$
 */
public class DerbyIdentityService implements IdentityService {
    
    private static final String checkCall = "{ call getlidforbid('a','b') }" ;

    private static final String getBIDStatement = "{ call getbid(?) }";
    
    private static final String getUIDStatement = "{ call getuid(?) }";

    private static final String getLIDForUIDStatement = "{ call getlidforuid(?) }";
                                                                
    private static final String getLIDForBIDStatement = "{ call getlidforbid(?,?) }";
    
    static{
        System.setProperty("derby.storage.pageCacheSize", "4000"); 
        System.setProperty("derby.storage.pageSize", "8192");
    }
    
    private final MiniConnectionPoolManager poolManager;
    
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
    public ID getID(LID lid) {
        long longVal = Long.parseLong(lid.getId());
        if (longVal % 2 == 0){
            return getUID(longVal);
        }else{
            return getBID(longVal);
        }
    }
    
    private BID getBID(long lid){
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try{
            conn = poolManager.getConnection();
            stmt = conn.prepareCall(getBIDStatement);
            stmt.setLong(1, lid);
            rs = stmt.executeQuery();
            if (rs.next()){
                String id = rs.getString(1);
                return new BID(id);
            }else{
                throw new IllegalArgumentException("No ID for " + lid);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(rs, stmt, conn);
        }                 
    }
    
    private UID getUID(long lid){
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try{
            conn = poolManager.getConnection();
            stmt = conn.prepareCall(getUIDStatement);
            stmt.setLong(1, lid);
            rs = stmt.executeQuery();
            if (rs.next()){
                String id = rs.getString(1);
                return new UID(id);
            }else{
                throw new IllegalArgumentException("No ID for " + lid);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(rs, stmt, conn);
        }
    }
    
    @Override
    public LID getLID(ID model, BID id) {
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try{
            conn = poolManager.getConnection();
            stmt = conn.prepareCall(getLIDForBIDStatement);
            stmt.setString(1, model.getId());
            stmt.setString(2, id.getId());  
            rs = stmt.executeQuery();
            if (rs.next()){
                long lid = rs.getLong(1);
                LID rv = new LID(lid);
                return rv;    
            }else{
                return null;
            }  
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(rs, stmt, conn);                    
        }         
    }
  
    @Override
    public LID getLID(UID id) {        
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try{
            conn = poolManager.getConnection();
            stmt = conn.prepareCall(getLIDForUIDStatement);
            stmt.setString(1, id.getId());   
            rs = stmt.executeQuery();
            if (rs.next()){
                long lid = rs.getLong(1);
                LID rv = new LID(lid);
                return rv;    
            }else{
                return null;
            }               
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
            JDBCUtil.safeClose(rs, stmt, conn);                    
        }    
    }
        
    @SuppressWarnings("unchecked")
    private void init() throws IOException {
        Connection connection = null;        
        Statement stmt = null;
        try{
            connection = poolManager.getConnection();
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
            connection = poolManager.getConnection();
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
