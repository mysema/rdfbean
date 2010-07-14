package com.mysema.rdfbean.rdb.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.joda.time.DateTime;

import com.mysema.query.sql.types.Type;

/**
 * @author tiwe
 *
 */
public class DateTimeType implements Type<DateTime>{

    @Override
    public Class<DateTime> getReturnedClass() {
        return DateTime.class;
    }

    @Override
    public int[] getSQLTypes() {
        return new int[]{Types.TIMESTAMP};
    }

    @Override
    public DateTime getValue(ResultSet rs, int startIndex) throws SQLException {
        Date date = rs.getTimestamp(startIndex);
        return date != null ? new DateTime(date) : null;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, DateTime value)
            throws SQLException {
        st.setTimestamp(startIndex, new Timestamp(value.getMillis()));
    }

}
