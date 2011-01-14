package com.mysema.rdfbean.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.SPARQLQueryBuilder;
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

    private final SPARQLQueryBuilder queryBuilder = new SPARQLQueryBuilder();
    
    @Nullable
    private CloseableIterator<Map<String,NODE>> tupleResults;
    
    private final List<String> variables = new ArrayList<String>();
    
    public SPARQLBeanQuery(Dialect<NODE, ID, BID, UID, LIT, STMT> dialect, 
            Session session, Configuration configuration, RDFConnection connection) {
        super(dialect, session, configuration);
        this.connection = connection;
    }

    @Override 
    protected <RT> RT convert(Class<RT> rt, LIT literal) {
        return conf.getConverterRegistry().fromString(literal.getValue(), rt);
    }

    @Override 
    protected Iterator<NODE[]> getInnerResults() {
        // TODO : populate query
        
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, queryBuilder.toString());
        tupleResults = query.getTuples();
        return new Iterator<NODE[]>(){
            @Override 
            public boolean hasNext() {
                return tupleResults.hasNext();
            }
            @Override 
            public NODE[] next() {
                NODE[] rv = new NODE[variables.size()];
                Map<String,NODE> tuples = tupleResults.next();
                for (int i = 0; i < variables.size(); i++){
                    rv[i] = tuples.get(variables.get(i));
                }
                return rv;
            }
            @Override 
            public void remove() {
                tupleResults.remove();                
            }            
        };        
    }

    @Override 
    public long count() {
        // TODO
        return 0;
    }

    @Override 
    public void close() throws IOException {
        if (tupleResults != null){
            tupleResults.close();
            tupleResults = null;
        }
    }    

}
