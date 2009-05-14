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
    
    static final String CONNECTION = "jdbc:default:connection";
   
    static final String GET_ID = "select id, is_uid from ids where lid = ?";
    
    static final String CREATE_ID = "insert into ids(model,id,is_uid) values(?,?,?)";
    
    static final String GET_LID1 = "select lid from ids where model = ? and id = ? and is_uid = ?";
    
    static final String GET_LID2 = "select lid from ids where model is null and id = ? and is_uid = ?";
    
    public static void getID(long lid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        try{
            PreparedStatement stmt = conn.prepareStatement(GET_ID);
            stmt.setLong(1, lid);
            data[0] = stmt.executeQuery();    
        }finally{
            JDBCUtil.safeClose(conn);
        }
    }
    
    public static void getLID(String model, String id, short is_uid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        try{
            data[0] = innerGetLID(conn, model, id, is_uid);
        }finally{
            JDBCUtil.safeClose(conn);
        }
    }
    
    public static void createLID(String model, String id, short is_uid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        PreparedStatement stmt = conn.prepareStatement(CREATE_ID);
        try{
            if (model != null){
                stmt.setString(1, model);    
            }else{
                stmt.setNull(1, Types.VARCHAR);
            }    
            stmt.setString(2, id);
            stmt.setShort(3, is_uid);
            stmt.executeUpdate();
            data[0] = innerGetLID(conn, model, id, is_uid);
        }finally{
            JDBCUtil.safeClose(null, stmt, conn);    
        }
    }

    private static ResultSet innerGetLID(Connection conn, String model, String id, short is_uid) throws SQLException {
        PreparedStatement stmt = null;
        if (model != null){
            stmt = conn.prepareStatement(GET_LID1);   
            stmt.setString(1, model);
            stmt.setString(2, id);
            stmt.setShort(3, is_uid);
        }else{
            stmt = conn.prepareStatement(GET_LID2);
            stmt.setString(1, id);
            stmt.setShort(2, is_uid);
        }        
        return stmt.executeQuery();
    }

}
