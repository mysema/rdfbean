/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * LuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConfiguration {
    
    private static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer(Version.LUCENE_30);
    
    private NodeConverter converter = NodeConverter.DEFAULT;
    
    private Analyzer analyzer = DEFAULT_ANALYZER;
    
    private Directory directory;
    
    private boolean fullTextIndexed = true;
    
    private boolean stored = true;
    
    private boolean contextsStored = true;
    
    public NodeConverter getConverter() {
        return converter;
    }

    public boolean isStored() {
        return stored;
    }

    public void setConverter(NodeConverter converter) {
        this.converter = converter;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public boolean isFullTextIndexed() {
        return fullTextIndexed;
    }

    public void setFullTextIndexed(boolean fullTextIndexed) {
        this.fullTextIndexed = fullTextIndexed;
    }

    public boolean isContextsStored() {
        return contextsStored;
    }

    public void setContextsStored(boolean contextsStored) {
        this.contextsStored = contextsStored;
    }
    
    
    
}
