/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.sql.*;

import com.mysema.util.JDBCUtil;

/**
 * DerbyProcedures contains procedure implementations used in the DerbyIdentityService
 *
 * @author tiwe
 * @version $Id$
 */
public final class DerbyProcedures {
    
    private static final String CONNECTION = "jdbc:default:connection";
    
    private static final String BID_CREATE_ID = "insert into bids(model, id) values(?, ?)";
   
    private static final String BID_GET_ID = "select id from bids where lid = ?";
    
    private static final String BID_GET_LID = "select lid from bids where model = ? and id = ?";
    
    private static final String UID_CREATE_ID = "insert into uids(id) values(?)";
    
    private static final String UID_GET_ID = "select id from uids where lid = ?";
    
    private static final String UID_GET_LID = "select lid from uids where id = ?";
    
    public static void createLID(String model, String id, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        PreparedStatement stmt = null; 
        try{
            if (model != null){
                stmt = conn.prepareStatement(BID_CREATE_ID);
                stmt.setString(1, model);
                stmt.setString(2, id);
                stmt.executeUpdate();
                data[0] = innerGetLID(conn, model, id);
            }else{
                stmt = conn.prepareStatement(UID_CREATE_ID);
                stmt.setString(1, id);
                stmt.executeUpdate();
                data[0] = innerGetLID(conn, id);
            }                
        }finally{
            JDBCUtil.safeClose(null, stmt, conn);    
        }
    }
    
    public static void getID(long lid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        PreparedStatement stmt = null;
        if (lid % 2 == 0){
            stmt = conn.prepareStatement(UID_GET_ID);    
        }else{
            stmt = conn.prepareStatement(BID_GET_ID);
        }        
        stmt.setLong(1, lid);
        data[0] = stmt.executeQuery();
        conn.close();
    }
    
    public static void getLID(String model, String id, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        if (model != null){
            data[0] = innerGetLID(conn, model, id);    
        }else{
            data[0] = innerGetLID(conn, id);
        }        
        conn.close();
    }

    private static ResultSet innerGetLID(Connection conn, String id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UID_GET_LID);
        stmt.setString(1, id);     
        return stmt.executeQuery();        
    }
    
    private static ResultSet innerGetLID(Connection conn, String model, String id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(BID_GET_LID);   
        stmt.setString(1, model);
        stmt.setString(2, id);      
        return stmt.executeQuery();        
    }

}
