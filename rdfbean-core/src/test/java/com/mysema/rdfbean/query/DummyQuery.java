package com.mysema.rdfbean.query;

import java.util.Collections;
import java.util.Iterator;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.Session;

public class DummyQuery extends AbstractProjectingQuery<DummyQuery,NODE,ID,BID,UID,LIT,STMT>{
    
    private NODE[] nodes = new NODE[]{new BID(),new BID()};

    public DummyQuery(Configuration configuration, Dialect<NODE, ID, BID, UID, LIT, STMT> dialect, Session session) {
	super(configuration, dialect, session);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <RT> RT convert(Class<RT> rt, LIT node) {
	if (rt.equals(Object.class)){
	    return (RT)new Object();    
	}else if (rt.equals(String.class)){
	    return (RT)"";
	}else{
	    throw new IllegalArgumentException("Unexpected type " + rt.getName());
	}
	
    }

    @Override
    protected Iterator<NODE[]> getInnerResults() {
	return Collections.singletonList(nodes).iterator();
    }

    @Override
    public long count() {
	return 0;
    }

}
