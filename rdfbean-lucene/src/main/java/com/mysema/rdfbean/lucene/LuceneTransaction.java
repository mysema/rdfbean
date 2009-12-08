/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.object.RDFBeanTransaction;

/**
 * LuceneTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneTransaction implements RDFBeanTransaction{
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneTransaction.class);
    
    private final IndexWriter writer;
    
    private boolean rollBackOnly;
    
    private boolean active = true;
    
    public LuceneTransaction(IndexWriter writer) {
        this.writer = Assert.notNull(writer);
    }

    @Override
    public void commit() {
        try {
            writer.commit();
        } catch (CorruptIndexException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            active = false;
        }    
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollBackOnly;
    }

    @Override
    public void rollback() {
        try {
            writer.rollback();
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            active = false;
        }
    }

    @Override
    public void setRollbackOnly() {
        this.rollBackOnly = true;
        
    }

}
