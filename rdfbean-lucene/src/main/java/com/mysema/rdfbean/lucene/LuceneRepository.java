/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
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
    
    private Directory directory;
    
    private NodeConverter nodeConverter = NodeConverter.DEFAULT;
    
    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    
    public LuceneRepository(){}
    
    public LuceneRepository(Directory directory, Analyzer analyzer){
        this.directory = Assert.notNull(directory);
        this.analyzer = Assert.notNull(analyzer);
    }
    
    @Override
    public void close() {        
        try {
            directory.close();
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
            IndexWriter writer = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
            IndexSearcher searcher = new IndexSearcher(directory);
            return new LuceneConnection(nodeConverter, writer, searcher);
        } catch (Exception e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    public void setDirectory(Directory directory) {
        this.directory = Assert.notNull(directory);
    }
    
}
