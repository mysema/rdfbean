package com.mysema.rdfbean.rdb;

import com.mysema.query.sql.SQLQuery;
import com.mysema.rdfbean.model.BooleanQuery;

public class BooleanQueryImpl implements BooleanQuery {

    private final SQLQuery query;
    
    public BooleanQueryImpl(SQLQuery query) {
        this.query = query;
    }

    @Override
    public boolean getBoolean() {
        return query.exists();
    }

}
