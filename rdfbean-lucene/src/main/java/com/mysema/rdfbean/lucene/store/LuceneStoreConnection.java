package com.mysema.rdfbean.lucene.store;

import static com.mysema.rdfbean.lucene.Constants.ALL_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_NULL;
import static com.mysema.rdfbean.lucene.Constants.ID_FIELD_NAME;

import java.util.List;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.CompassQueryBuilder.CompassBooleanQueryBuilder;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.lucene.AbstractLuceneConnection;
import com.mysema.rdfbean.lucene.LuceneConfiguration;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.EmptyCloseableIterator;

/**
 * LuceneStoreConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneStoreConnection extends AbstractLuceneConnection{
    
    public LuceneStoreConnection(LuceneConfiguration configuration, CompassSession session) {
        super(configuration, session);
    }
    
    private CompassQuery createQuery(ID subject, UID predicate, NODE object, UID context){
        CompassQueryBuilder queryBuilder = compassSession.queryBuilder();
        if (subject != null || predicate != null || object != null || context != null){            
            CompassBooleanQueryBuilder boolBuilder = queryBuilder.bool();
            if (subject != null){
                boolBuilder.addMust(queryBuilder.term(ID_FIELD_NAME, conf.getConverter().toString(subject)));
                // TODO : component id matches need to be handled here as well
            }   
            if (predicate != null){
                String predicateField = conf.getConverter().uidToShortString(predicate);
                // TODO : component predicate matches need to be handled here
                if (object != null){
                    String value = conf.getConverter().toString(object);
                    boolBuilder.addMust(queryBuilder.term(predicateField, value));    
                }else{
                    boolBuilder.addMust(queryBuilder.wildcard(predicateField, "*"));
                }
                
            }else if (object != null){
                String value = conf.getConverter().toString(object);
                boolBuilder.addMust(queryBuilder.term(ALL_FIELD_NAME, value));
            }
            
            if (conf.isContextsStored()){
                if (context != null){
                    String value = conf.getConverter().toString(context);
                    boolBuilder.addMust(queryBuilder.term(CONTEXT_FIELD_NAME, value));
                }else{
                    boolBuilder.addMust(queryBuilder.term(CONTEXT_FIELD_NAME, CONTEXT_NULL));
                }
            }
                                   
            return boolBuilder.toQuery();
            
        }else{
            return queryBuilder.matchAll();
        }
    }
        
    @Override
    public CloseableIterator<STMT> findStatements(final ID subject, final UID predicate, final NODE object, 
            final UID context, boolean includeInferred) {        
        CompassQuery query = createQuery(subject, predicate, object, context);        
        CompassHits hits = query.hits();        
        if (hits.getLength() > 0){
            return new ResultIterator(hits){
                @Override
                protected List<STMT> getStatements(Resource resource) {
                    return findStatements(resource, subject, predicate, object, context);
                }                
            };
        }else{
            hits.close();
            return new EmptyCloseableIterator<STMT>();
        }
    }

    
}
