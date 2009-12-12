/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.index;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.lucene.AbstractLuceneConnection;
import com.mysema.rdfbean.lucene.LuceneConfiguration;
import com.mysema.rdfbean.lucene.LuceneQuery;
import com.mysema.rdfbean.lucene.LuceneRepository;
import com.mysema.rdfbean.model.MultiConnection;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Session;

/**
 * LuceneEnhancedRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneEnhancedRepository implements Repository{
    
    private LuceneConfiguration configuration;
    
    private LuceneRepository luceneRepository;
    
    private Repository repository;

    public LuceneEnhancedRepository(){}
    
    public LuceneEnhancedRepository(Repository repository, LuceneConfiguration configuration) {
        this.repository = Assert.notNull(repository);
        this.configuration = Assert.notNull(configuration);
    }
    
    @Override
    public void close() {
        repository.close();
        luceneRepository.close();        
    }

    @Override
    public void initialize() {
        luceneRepository = new LuceneRepository();
        luceneRepository.setConfiguration(configuration);
        luceneRepository.initialize();        
        repository.initialize();        
    }

    @Override
    public RDFConnection openConnection() {
        final AbstractLuceneConnection luceneConnection = luceneRepository.openConnection();
        final RDFConnection connection = repository.openConnection();
        return new MultiConnection(connection, luceneConnection){
            @Override
            public <Q> Q createQuery(Session session, Class<Q> queryType) {
                if (queryType.equals(LuceneQuery.class)){
                    return luceneConnection.createQuery(session, queryType);
                }else{
                    return connection.createQuery(session, queryType);
                }
            }
        };
    }
    
    public void setConfiguration(LuceneConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setRepository(Repository repository){
        this.repository = Assert.notNull(repository);
    }
}
