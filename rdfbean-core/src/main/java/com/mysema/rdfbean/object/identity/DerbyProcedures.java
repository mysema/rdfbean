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
        
    public static void getLIDForBID(String model, String id, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        PreparedStatement stmt = null; 
        try{
            PreparedStatement s = conn.prepareStatement(BID_GET_LID,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            s.setString(1, model);
            s.setString(2, id);      
            ResultSet rs = s.executeQuery(); 
            if (rs.next()){    
                rs.beforeFirst();
                data[0] = rs;
            }else{
                stmt = conn.prepareStatement(BID_CREATE_ID);
                stmt.setString(1, model);
                stmt.setString(2, id);
                stmt.executeUpdate();
                stmt.close();   
                data[0] = s.executeQuery();
            }   
        }finally{
            JDBCUtil.safeClose(null, null, conn);
        }        
    }
    
    public static void getLIDForUID(String id, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        PreparedStatement stmt = null; 
        try{
            PreparedStatement s = conn.prepareStatement(UID_GET_LID,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            s.setString(1, id);     
            ResultSet rs = s.executeQuery();        
            if (rs.next()){
                rs.beforeFirst();
                data[0] = rs;
            }else{
                stmt = conn.prepareStatement(UID_CREATE_ID);
                stmt.setString(1, id);
                stmt.executeUpdate();
                stmt.close();                     
                data[0] = s.executeQuery();
            }            
        }finally{
            JDBCUtil.safeClose(null, stmt, conn);    
        }
    }
    
    public static void getBID(long lid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        try{
            PreparedStatement stmt = conn.prepareStatement(BID_GET_ID);
            stmt.setLong(1, lid);
            data[0] = stmt.executeQuery();    
        }finally{
            conn.close();    
        }
    }
    
    public static void getUID(long lid, ResultSet[] data) throws SQLException{
        Connection conn = DriverManager.getConnection(CONNECTION);
        try{
            PreparedStatement stmt = conn.prepareStatement(UID_GET_ID);
            stmt.setLong(1, lid);
            data[0] = stmt.executeQuery();    
        }finally{
            conn.close();    
        }
    }
    
}
