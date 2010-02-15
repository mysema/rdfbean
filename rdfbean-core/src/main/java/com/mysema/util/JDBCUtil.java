/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nullable;

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
    
    public static void safeClose(@Nullable ResultSet rs) {
        try {
            if (rs != null) rs.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }        
    }
    
    public static void safeClose(@Nullable Statement stmt) {
        try {
            if (stmt != null) stmt.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public static void safeClose(@Nullable Connection conn) {
        try {
            if (conn != null) conn.close();            
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public static void safeClose(@Nullable ResultSet rs, @Nullable Statement stmt, @Nullable Connection conn) {
        safeClose(rs);
        safeClose(stmt);
        safeClose(conn);
    }

}
