/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.JDBCUtil;

/**
 * JDBCIdentityService provides an abstract stored procedure based
 * implementation of the IdentityService interface
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class JDBCIdentityService implements IdentityService{
    
    private String createLIDStatement = "{ call createlid(?,?) }";
    
    private String getIDStatement = "{ call getid(?) }";
    
    private String getLIDStatement = "{ call getlid(?,?) }";
    
    private Map<LID,ID> idMap = Collections.synchronizedMap(new HashMap<LID,ID>());
    
    private Map<IDKey,LID> lidMap = Collections.synchronizedMap(new HashMap<IDKey,LID>());
    
    public JDBCIdentityService(){}
    
    public JDBCIdentityService(String getid, String getlid, String createlid){
        this();
        this.getIDStatement = Assert.hasText(getid);
        this.getLIDStatement = Assert.hasText(getlid);
        this.createLIDStatement = Assert.hasText(createlid);
    }
 
    protected abstract Connection getConnection() throws SQLException;
    
    @Override
    public ID getID(LID lid) {
        if (idMap.containsKey(lid)){
            return idMap.get(lid);
        }else{
            Connection conn = null;
            CallableStatement stmt = null;
            ResultSet rs = null;
            try{
                conn = getConnection();
                stmt = conn.prepareCall(getIDStatement);
                long longVal = Long.valueOf(lid.getId());
                stmt.setLong(1, longVal);
                rs = stmt.executeQuery();
                if (rs.next()){
                    String id = rs.getString(1);
                    ID rv;
                    if (longVal % 2 == 0){
                        rv = new UID(id);
                    }else{
                        rv = new BID(id);
                    }
                    idMap.put(lid, rv);
                    return rv;
                }else{
                    throw new IllegalArgumentException("No ID for " + lid);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }finally{
                JDBCUtil.safeClose(rs, stmt, conn);
            }    
        }                    
    }
    
    
    @Override
    public LID getLID(@Nullable ID model, ID id) {
        if (id instanceof UID){
            model = null;
        }
        IDKey key = new IDKey(model, id);
        if (lidMap.containsKey(key)){
            return lidMap.get(key);
        }else{
            Connection conn = null;
            CallableStatement stmt = null;
            ResultSet rs = null;
            try{
                conn = getConnection();
                stmt = prepareForGetCreateLID(getLIDStatement, model, id, conn);
                rs = stmt.executeQuery();
                // already exists
                if (rs.next()){
                    long lid = rs.getLong(1);
                    LID rv = new LID(lid);
                    lidMap.put(key, rv);        
                    return rv;
                }else{
                    JDBCUtil.safeClose(rs, stmt, null);
                    stmt = prepareForGetCreateLID(createLIDStatement, model, id, conn);
                    rs = stmt.executeQuery();    
                    if (rs.next()){
                        long lid = rs.getLong(1);
                        LID rv = new LID(lid);
                        lidMap.put(key, rv);
                        return rv;
                    }else{        
                        throw new IllegalArgumentException("No LID for " + id + " (" + model + ")");
                    }
                }            
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }finally{
                JDBCUtil.safeClose(rs, stmt, conn);                    
            }     
        }        
    }
    
    @Override
    public LID getLID(UID id) {        
        return getLID(null, id);
    }

    private CallableStatement prepareForGetCreateLID(String call, ID model, ID id, Connection conn) throws SQLException {
        CallableStatement stmt;
        stmt = conn.prepareCall(call);
        if (model != null){
            stmt.setString(1, model.getId());    
        }else{
            stmt.setNull(1, Types.VARCHAR);
        }                
        stmt.setString(2, id.getId());    
        return stmt;
    }
    
}
