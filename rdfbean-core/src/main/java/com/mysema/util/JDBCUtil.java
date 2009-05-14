/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBCUtil provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JDBCUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
    
    public static void safeClose(ResultSet rs) {
        try {
            if (rs != null) rs.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }        
    }
    
    public static void safeClose(Statement stmt) {
        try {
            if (stmt != null) stmt.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public static void safeClose(Connection conn) {
        try {
            if (conn != null) conn.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public static void safeClose(ResultSet rs, Statement stmt, Connection conn) {
        safeClose(rs);
        safeClose(stmt);
        safeClose(conn);
    }

}
