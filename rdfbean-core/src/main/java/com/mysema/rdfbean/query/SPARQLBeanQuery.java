package com.mysema.rdfbean.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class SPARQLBeanQuery 
    extends AbstractProjectingQuery<SPARQLBeanQuery, NODE, ID, BID, UID, LIT, STMT>
    implements BeanQuery, Closeable{
    
    private final RDFConnection connection;

    public SPARQLBeanQuery(Dialect<NODE, ID, BID, UID, LIT, STMT> dialect, Session session, Configuration configuration, RDFConnection connection) {
        super(dialect, session, configuration);
        this.connection = connection;
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

    @Override 
    public void close() throws IOException {
        // TODO Auto-generated method stub        
    }    

}
