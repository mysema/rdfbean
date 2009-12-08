/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Repository;

/**
 * LuceneRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepository implements Repository{

    private static final Logger logger = LoggerFactory.getLogger(LuceneRepository.class);
    
    private LuceneConfiguration configuration;
    
    public LuceneRepository(){}
    
    public LuceneRepository(LuceneConfiguration configuration){
        this.configuration = Assert.notNull(configuration);
    }
    
    @Override
    public void close() {        
        try {
            configuration.getDirectory().close();
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }        
    }

    @Override
    public void initialize() {
        // TODO        
    }

    @Override
    public LuceneConnection openConnection() {
        try {
            IndexWriter writer = new IndexWriter(
                    configuration.getDirectory(), 
                    configuration.getAnalyzer(),
                    IndexWriter.MaxFieldLength.UNLIMITED);
            IndexSearcher searcher = new IndexSearcher(configuration.getDirectory());
            return new LuceneConnection(configuration, writer, searcher);
        } catch (Exception e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    public void setConfiguration(LuceneConfiguration configuration) {
        this.configuration = configuration;
    }
    
}
