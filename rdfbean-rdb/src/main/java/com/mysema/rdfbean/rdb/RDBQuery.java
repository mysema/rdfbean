package com.mysema.rdfbean.rdb;

import java.util.Iterator;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniDialect;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.query.AbstractProjectingQuery;

/**
 * @author tiwe
 *
 */
public class RDBQuery extends AbstractProjectingQuery<RDBQuery, NODE, ID, BID, UID, LIT, STMT>{

    public RDBQuery(Session session) {
        super(new MiniDialect(), session);
    }

    @Override
    protected <RT> RT convert(Class<RT> rt, LIT node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Iterator<NODE[]> getInnerResults() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

}
