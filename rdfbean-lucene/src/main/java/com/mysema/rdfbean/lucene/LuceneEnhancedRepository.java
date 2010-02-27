/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.OutputStream;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.MultiConnection;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.io.Format;
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
        final LuceneConnection luceneConnection = luceneRepository.openConnection();
        final RDFConnection connection = repository.openConnection();
        return new MultiConnection(connection, luceneConnection){
            @Override
            public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
                if (queryLanguage.equals(Constants.LUCENEQUERY) || queryLanguage.equals(Constants.COMPASSQUERY)){
                    return luceneConnection.createQuery(session, queryLanguage, definition);
                }else{
                    return connection.createQuery(session, queryLanguage, definition);
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

    @Override
    public void export(Format format, OutputStream os) {
        repository.export(format, os);        
    }

}
