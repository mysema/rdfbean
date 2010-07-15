/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.support;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.LocalDate;

import com.mysema.query.sql.types.Type;

/**
 * @author tiwe
 *
 */
public class LocalDateType implements Type<LocalDate>{

    @Override
    public Class<LocalDate> getReturnedClass() {
        return LocalDate.class;
    }

    @Override
    public int[] getSQLTypes() {
        return new int[]{Types.DATE};
    }

    @Override
    public LocalDate getValue(ResultSet rs, int startIndex) throws SQLException {
        Date date = rs.getDate(startIndex);
        return date != null ? new LocalDate(date) : null;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, LocalDate value)
            throws SQLException {
        st.setDate(startIndex, new Date(value.toDateMidnight().getMillis()));
        
    }

}
